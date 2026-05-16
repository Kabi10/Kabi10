import * as functions from "firebase-functions";
import { initializeApp } from "firebase-admin/app";
import { getFirestore } from "firebase-admin/firestore";
import { getMessaging } from "firebase-admin/messaging";

initializeApp();

// Authorized emails for callable functions
const ALLOW = new Set([
  "kabiedu@gmail.com",
  "kabilan321@gmail.com", 
  "senthuja30@gmail.com"
]);

/**
 * Security function to check if user is authorized
 */
function assertAllowed(context: functions.https.CallableContext) {
  const email = context.auth?.token?.email as string | undefined;
  if (!email || !ALLOW.has(email)) {
    throw new functions.https.HttpsError("permission-denied", "Not allowed");
  }
}

/**
 * Utility function to get device token from Firestore
 */
async function tokenFor(deviceId: string): Promise<string> {
  const ref = getFirestore().doc(`devices/${deviceId}`);
  const snap = await ref.get();
  const token = snap.get("token");
  if (!token) {
    throw new functions.https.HttpsError("not-found", "No token");
  }
  return token as string;
}

/**
 * Callable function to send SNAP command
 */
export const sendSnap = functions.https.onCall(async (data, context) => {
  assertAllowed(context);
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
  assertAllowed(context);
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
  assertAllowed(context);
  const deviceId = String(data.deviceId || "primary");
  const token = await tokenFor(deviceId);
  
  await getMessaging().send({
    token,
    data: { cmd: "LIST_APPS" },
    android: { priority: "high" as const }
  });
  
  return { ok: true };
});

// Note: Firestore triggers and scheduled functions temporarily disabled
// until Firestore database is properly configured
// 
// Uncomment these functions after:
// 1. Creating Firestore database via Firebase Console
// 2. Setting up proper service account permissions
//
// export const onDeviceUpdate = functions.firestore...
// export const cleanupInactiveDevices = functions.pubsub...