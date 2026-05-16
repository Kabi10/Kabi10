import * as functions from "firebase-functions";
import { initializeApp } from "firebase-admin/app";
import { getFirestore } from "firebase-admin/firestore";
import { getMessaging } from "firebase-admin/messaging";

initializeApp();

// Replace with your actual email address
const AUTHORIZED_EMAIL = "kabilan321@gmail.com";

/**
 * Security function to check if user is authorized
 */
function assertOwner(context: functions.https.CallableContext) {
  if (!context.auth || context.auth.token.email !== AUTHORIZED_EMAIL) {
    throw new functions.https.HttpsError("permission-denied", "Not allowed");
  }
}

/**
 * Utility function to get device token from Firestore
 */
async function tokenFor(deviceId: string): Promise<string> {
  const doc = await getFirestore().doc(`devices/${deviceId}`).get();
  const token = doc.get("token");
  if (!token) {
    throw new functions.https.HttpsError("not-found", "No token");
  }
  return token;
}

/**
 * Callable function to send SNAP command
 */
export const sendSnap = functions.https.onCall(async (data, context) => {
  assertOwner(context);
  const deviceId = String(data.deviceId || "primary");
  const token = await tokenFor(deviceId);
  
  await getMessaging().send({
    token,
    data: { cmd: "SNAP" },
    android: { priority: "high" as const }
  });
  
  return { ok: true };
});

/**
 * Callable function to send LAUNCH command
 */
export const sendLaunch = functions.https.onCall(async (data, context) => {
  assertOwner(context);
  const deviceId = String(data.deviceId || "primary");
  const pkg = String(data.pkg || "");
  
  if (!pkg) {
    throw new functions.https.HttpsError("invalid-argument", "pkg required");
  }
  
  const token = await tokenFor(deviceId);
  
  await getMessaging().send({
    token,
    data: { cmd: "LAUNCH", pkg },
    android: { priority: "high" as const }
  });
  
  return { ok: true };
});

/**
 * Callable function to request app report
 */
export const requestAppReport = functions.https.onCall(async (data, context) => {
  assertOwner(context);
  const deviceId = String(data.deviceId || "primary");
  const token = await tokenFor(deviceId);
  
  await getMessaging().send({
    token,
    data: { cmd: "LIST_APPS" },
    android: { priority: "high" as const }
  });
  
  return { ok: true };
});

/**
 * Firestore trigger for device updates
 */
export const onDeviceUpdate = functions.firestore
  .document("devices/{deviceId}")
  .onWrite(async (change, context) => {
    const deviceId = context.params.deviceId;
    
    if (!change.after.exists) {
      functions.logger.info(`Device ${deviceId} deleted`);
      return;
    }
    
    const newData = change.after.data();
    const previousData = change.before.exists ? change.before.data() : null;
    
    if (!previousData) {
      functions.logger.info(`New device registered: ${deviceId}`);
    } else if (previousData.token !== newData?.token) {
      functions.logger.info(`Device token updated: ${deviceId}`);
    }
  });

/**
 * Scheduled cleanup function
 */
export const cleanupInactiveDevices = functions.pubsub
  .schedule("0 2 * * *")
  .timeZone("UTC")
  .onRun(async (context) => {
    functions.logger.info("Starting device cleanup");
    
    const firestore = getFirestore();
    const cutoffDate = new Date();
    cutoffDate.setDate(cutoffDate.getDate() - 30);
    
    const snapshot = await firestore
      .collection("devices")
      .where("isActive", "==", false)
      .where("lastUpdated", "<", cutoffDate.toISOString().split("T")[0])
      .get();
    
    if (snapshot.empty) {
      functions.logger.info("No inactive devices to clean");
      return;
    }
    
    const batch = firestore.batch();
    snapshot.docs.forEach((doc) => batch.delete(doc.ref));
    await batch.commit();
    
    functions.logger.info(`Cleaned up ${snapshot.size} inactive devices`);
  });