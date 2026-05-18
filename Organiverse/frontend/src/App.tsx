import {
  Activity,
  BookOpen,
  Brain,
  Check,
  Clipboard,
  Cloud,
  CloudLightning,
  Copy,
  Download,
  HeartPulse,
  KeyRound,
  Moon,
  Newspaper,
  PanelRightClose,
  PanelRightOpen,
  RotateCcw,
  Send,
  Settings,
  SlidersHorizontal,
  Soup,
  Sun,
  Trophy,
  Users,
  Volume2,
  VolumeX,
  Wifi,
  WifiOff,
  Wind,
  Zap,
} from "lucide-react";
import {
  type FormEvent,
  useCallback,
  useEffect,
  useMemo,
  useRef,
  useState,
} from "react";

type Role = "user" | "organ" | "report" | "system";
type PanelTab = "status" | "diary" | "settings";
type Verbosity = "terse" | "normal" | "verbose";
type Theme = "dark" | "light";

type Message = {
  id: number | string;
  sessionId?: string;
  senderId: string;
  senderName: string;
  role: Role | string;
  kind?: string;
  content: string;
  color: string;
  avatar: string;
  metadata?: Record<string, unknown>;
  createdAt?: string;
};

type Organ = {
  id: string;
  name: string;
  avatar: string;
  color: string;
  specialty?: string;
};

type Relationship = {
  source: string;
  target: string;
  score: number;
};

type WeatherState = {
  id: string;
  label: string;
  intensity: number;
};

type SessionSettings = {
  enabledOrgans: string[];
  verbosity: Verbosity;
  soundEnabled: boolean;
  achievementsEnabled: boolean;
  theme: Theme;
  volume: number;
};

type Achievement = {
  id: string;
  title: string;
  organId: string;
  message: string;
  unlockedAt?: string;
};

type DiaryEntry = {
  organId: string;
  organName: string;
  color: string;
  entry: string;
};

type SessionState = {
  sessionId: string;
  createdAt: string;
  updatedAt: string;
  relationships: Relationship[];
  moods: Record<string, number>;
  eventCount: number;
  crisisCount: number;
  rebellionActive: boolean;
  crisisActive?: boolean;
  memorySummary: string;
  weather: WeatherState;
  achievements: Achievement[];
  settings: SessionSettings;
};

type Summary = {
  title: string;
  subtitle?: string;
  paragraphs: Array<{
    organId: string;
    organName: string;
    text: string;
  }>;
};

type Toast = Achievement & { toastId: string };

type AudioWindow = Window &
  typeof globalThis & {
    webkitAudioContext?: typeof AudioContext;
  };

const SESSION_KEY = "organiverse-session-id";
const API_KEY_KEY = "organiverse-deepseek-api-key";
const CLIENT_NAME_KEY = "organiverse-client-name";
const SEND_DEBOUNCE_MS = 500;

const QUICK_ACTIONS = [
  { label: "3am anxiety spiral", fill: "3am anxiety spiral" },
  { label: "guided breathing", fill: "@Lungs walk me through breathing" },
  { label: "meal planning", fill: "@Stomach plan dinner" },
  { label: "hydration win", fill: "drank water after coffee" },
  { label: "heart advice", fill: "@Heart should I text them back?" },
  { label: "liver roast", fill: "@Liver roast my choices today" },
];

const FALLBACK_ORGANS: Organ[] = [
  { id: "heart", name: "Heart", avatar: "H", color: "#e11d48", specialty: "Relationship advice" },
  { id: "brain", name: "Brain", avatar: "B", color: "#7c3aed", specialty: "Decision analysis" },
  { id: "liver", name: "Liver", avatar: "Lv", color: "#b45309", specialty: "Brutal feedback" },
  { id: "lungs", name: "Lungs", avatar: "Lu", color: "#0891b2", specialty: "Guided breathing" },
  { id: "stomach", name: "Stomach", avatar: "S", color: "#16a34a", specialty: "Meal planning" },
  { id: "kidneys", name: "Kidneys", avatar: "K", color: "#0f766e", specialty: "Hydration check-in" },
];

const ORGAN_IDS = FALLBACK_ORGANS.map((organ) => organ.id);

const DEFAULT_SETTINGS: SessionSettings = {
  enabledOrgans: ORGAN_IDS,
  verbosity: "normal",
  soundEnabled: false,
  achievementsEnabled: true,
  theme: "dark",
  volume: 0.35,
};

const SPECIALTY_COPY: Record<string, string> = {
  heart: "Relationship advice mode. Heart takes this personally.",
  brain: "Decision analysis mode. Brain will turn it into neural logistics.",
  liver: "Brutal feedback mode. Liver is already tired.",
  lungs: "Anxiety and breathing mode. Lungs will narrate every inhale.",
  stomach: "Meal planning mode. Stomach has obvious priorities.",
  kidneys: "Hydration intervention mode. Kidneys get the floor.",
};

function classNames(...values: Array<string | false | null | undefined>) {
  return values.filter(Boolean).join(" ");
}

function createRoomCode() {
  const alphabet = "ABCDEFGHJKLMNPQRSTUVWXYZ23456789";
  let code = "BODY-";
  const values = new Uint32Array(6);
  if ("crypto" in window && "getRandomValues" in crypto) {
    crypto.getRandomValues(values);
    values.forEach((value) => {
      code += alphabet[value % alphabet.length];
    });
    return code;
  }
  return `BODY-${Math.random().toString(36).slice(2, 8).toUpperCase()}`;
}

function sanitizeRoomCode(value: string | null | undefined) {
  return (value ?? "")
    .trim()
    .replace(/[^a-zA-Z0-9_-]/g, "")
    .slice(0, 48);
}

function getInitialSessionId() {
  const params = new URL(window.location.href).searchParams;
  const fromUrl = sanitizeRoomCode(params.get("room") ?? params.get("session"));
  if (fromUrl) {
    localStorage.setItem(SESSION_KEY, fromUrl);
    return fromUrl;
  }

  const existing = sanitizeRoomCode(localStorage.getItem(SESSION_KEY));
  if (existing) {
    return existing;
  }

  const next = createRoomCode();
  localStorage.setItem(SESSION_KEY, next);
  return next;
}

function getClientName() {
  const existing = localStorage.getItem(CLIENT_NAME_KEY);
  if (existing) {
    return existing;
  }
  const next = `User ${Math.floor(100 + Math.random() * 900)}`;
  localStorage.setItem(CLIENT_NAME_KEY, next);
  return next;
}

function getDeploymentBasePath() {
  const configured = import.meta.env.BASE_URL as string | undefined;
  if (!configured || configured === "/") {
    return "";
  }
  const trimmed = configured.replace(/^\/+|\/+$/g, "");
  return trimmed ? `/${trimmed}` : "";
}

function withDeploymentBase(path: string) {
  const normalized = path.startsWith("/") ? path : `/${path}`;
  return `${getDeploymentBasePath()}${normalized}`;
}

function isCompactViewport() {
  return window.matchMedia("(max-width: 900px)").matches;
}

function getApiBase() {
  const configured = import.meta.env.VITE_API_URL as string | undefined;
  if (configured) {
    return configured.replace(/\/$/, "");
  }
  if (window.location.port === "5173") {
    return `${window.location.protocol}//${window.location.hostname}:8000`;
  }
  return `${window.location.origin}${getDeploymentBasePath()}`;
}

function getWebSocketUrl(sessionId: string) {
  const protocol = window.location.protocol === "https:" ? "wss" : "ws";
  if (window.location.port === "5173") {
    const configured = import.meta.env.VITE_WS_URL as string | undefined;
    if (configured) {
      return `${configured.replace(/\/$/, "")}/${encodeURIComponent(sessionId)}`;
    }
    return `ws://${window.location.hostname}:8000/ws/${encodeURIComponent(sessionId)}`;
  }
  return `${protocol}://${window.location.host}${withDeploymentBase(`/ws/${encodeURIComponent(sessionId)}`)}`;
}

function updateRoomUrl(sessionId: string) {
  const url = new URL(window.location.href);
  url.searchParams.set("room", sessionId);
  window.history.replaceState(null, "", url);
}

function defaultRelationships(): Relationship[] {
  const relationships: Relationship[] = [];
  ORGAN_IDS.forEach((source, index) => {
    ORGAN_IDS.slice(index + 1).forEach((target) => {
      let score = 0;
      if ((source === "brain" && target === "stomach") || (source === "stomach" && target === "brain")) {
        score = -20;
      }
      if ((source === "heart" && target === "lungs") || (source === "lungs" && target === "heart")) {
        score = 15;
      }
      if ((source === "liver" && target === "kidneys") || (source === "kidneys" && target === "liver")) {
        score = 10;
      }
      relationships.push({ source, target, score });
    });
  });
  return relationships;
}

function normalizeRelationships(value: unknown, fallback: Relationship[]) {
  if (Array.isArray(value)) {
    return value
      .map((item) => {
        if (!item || typeof item !== "object") {
          return null;
        }
        const source = String((item as Relationship).source ?? "");
        const target = String((item as Relationship).target ?? "");
        const score = Number((item as Relationship).score ?? 0);
        if (!source || !target) {
          return null;
        }
        return { source, target, score: Math.max(-100, Math.min(100, score)) };
      })
      .filter((item): item is Relationship => Boolean(item));
  }

  if (value && typeof value === "object") {
    return Object.entries(value as Record<string, unknown>)
      .map(([key, score]) => {
        const [source, target] = key.split("|");
        if (!source || !target) {
          return null;
        }
        return {
          source,
          target,
          score: Math.max(-100, Math.min(100, Number(score ?? 0))),
        };
      })
      .filter((item): item is Relationship => Boolean(item));
  }

  return fallback;
}

function normalizeSettings(value: unknown): SessionSettings {
  const raw = value && typeof value === "object" ? (value as Partial<SessionSettings>) : {};
  const verbosity = ["terse", "normal", "verbose"].includes(String(raw.verbosity))
    ? (raw.verbosity as Verbosity)
    : DEFAULT_SETTINGS.verbosity;
  const theme = raw.theme === "light" ? "light" : "dark";
  const enabledOrgans = Array.isArray(raw.enabledOrgans)
    ? raw.enabledOrgans.filter((id): id is string => typeof id === "string")
    : DEFAULT_SETTINGS.enabledOrgans;

  return {
    enabledOrgans: enabledOrgans.length ? enabledOrgans : DEFAULT_SETTINGS.enabledOrgans,
    verbosity,
    soundEnabled: Boolean(raw.soundEnabled),
    achievementsEnabled:
      typeof raw.achievementsEnabled === "boolean"
        ? raw.achievementsEnabled
        : DEFAULT_SETTINGS.achievementsEnabled,
    theme,
    volume: Math.max(0, Math.min(1, Number(raw.volume ?? DEFAULT_SETTINGS.volume))),
  };
}

function createInitialState(sessionId: string): SessionState {
  const now = new Date().toISOString();
  return {
    sessionId,
    createdAt: now,
    updatedAt: now,
    relationships: defaultRelationships(),
    moods: Object.fromEntries(ORGAN_IDS.map((id) => [id, 0])),
    eventCount: 0,
    crisisCount: 0,
    rebellionActive: false,
    memorySummary: "No user events logged yet.",
    weather: { id: "clear", label: "Clear Skies", intensity: 12 },
    achievements: [],
    settings: DEFAULT_SETTINGS,
  };
}

function normalizeState(sessionId: string, incoming: unknown, previous?: SessionState): SessionState {
  const base = previous ?? createInitialState(sessionId);
  if (!incoming || typeof incoming !== "object") {
    return base;
  }
  const raw = incoming as Partial<SessionState>;
  return {
    ...base,
    ...raw,
    sessionId: String(raw.sessionId ?? sessionId),
    createdAt: String(raw.createdAt ?? base.createdAt),
    updatedAt: String(raw.updatedAt ?? new Date().toISOString()),
    relationships: normalizeRelationships(raw.relationships, base.relationships),
    moods:
      raw.moods && typeof raw.moods === "object"
        ? { ...base.moods, ...(raw.moods as Record<string, number>) }
        : base.moods,
    eventCount: Number(raw.eventCount ?? base.eventCount),
    crisisCount: Number(raw.crisisCount ?? base.crisisCount),
    rebellionActive: Boolean(raw.rebellionActive),
    crisisActive: Boolean(raw.crisisActive),
    memorySummary: String(raw.memorySummary ?? base.memorySummary),
    weather:
      raw.weather && typeof raw.weather === "object"
        ? {
            id: String(raw.weather.id ?? base.weather.id),
            label: String(raw.weather.label ?? base.weather.label),
            intensity: Number(raw.weather.intensity ?? base.weather.intensity),
          }
        : base.weather,
    achievements: Array.isArray(raw.achievements) ? raw.achievements : base.achievements,
    settings: normalizeSettings(raw.settings ?? base.settings),
  };
}

function getOrganById(organs: Organ[], organId: string) {
  return (
    organs.find((organ) => organ.id === organId) ??
    FALLBACK_ORGANS.find((organ) => organ.id === organId) ?? {
      id: organId,
      name: organId,
      avatar: organId.slice(0, 1).toUpperCase(),
      color: "#64748b",
    }
  );
}

function getMention(content: string) {
  const match = content.match(/^\s*@([a-zA-Z]+)\b/);
  if (!match) {
    return null;
  }
  const organ = FALLBACK_ORGANS.find(
    (item) => item.id.toLowerCase() === match[1].toLowerCase() || item.name.toLowerCase() === match[1].toLowerCase(),
  );
  return organ ?? null;
}

function getMoodLabel(score: number) {
  if (score >= 25) return "thriving";
  if (score >= 8) return "steady";
  if (score <= -35) return "mutiny";
  if (score <= -14) return "strained";
  return "neutral";
}

function getMoodPercent(score: number) {
  return Math.max(0, Math.min(100, 50 + score / 2));
}

function getVisualWeather(state: SessionState): WeatherState {
  if (state.rebellionActive) {
    return { id: "hurricane", label: "Hurricane", intensity: 100 };
  }
  if (state.crisisActive || state.weather.id === "tornado") {
    return { id: "tornado", label: "Tornado", intensity: 94 };
  }
  return state.weather;
}

function formatDuration(fromIso: string, now: number) {
  const start = Date.parse(fromIso);
  const elapsed = Number.isFinite(start) ? Math.max(0, now - start) : 0;
  const totalSeconds = Math.floor(elapsed / 1000);
  const hours = Math.floor(totalSeconds / 3600);
  const minutes = Math.floor((totalSeconds % 3600) / 60);
  const seconds = totalSeconds % 60;
  if (hours > 0) {
    return `${hours}h ${String(minutes).padStart(2, "0")}m`;
  }
  return `${minutes}m ${String(seconds).padStart(2, "0")}s`;
}

function formatTime(value?: string) {
  const date = value ? new Date(value) : null;
  if (!date || Number.isNaN(date.getTime())) {
    return "now";
  }
  return date.toLocaleTimeString([], { hour: "numeric", minute: "2-digit" });
}

function buildShareUrl(sessionId: string) {
  const url = new URL(window.location.href);
  url.searchParams.set("room", sessionId);
  return url.toString();
}

function useSyntheticAudio(settings: SessionSettings, weather: WeatherState, rebellionActive: boolean) {
  const contextRef = useRef<AudioContext | null>(null);
  const heartbeatRef = useRef<number | null>(null);

  const ensureContext = useCallback(() => {
    const audioWindow = window as AudioWindow;
    const AudioContextClass = audioWindow.AudioContext ?? audioWindow.webkitAudioContext;
    if (!AudioContextClass) {
      return null;
    }
    if (!contextRef.current) {
      contextRef.current = new AudioContextClass();
    }
    void contextRef.current.resume();
    return contextRef.current;
  }, []);

  const tone = useCallback(
    (frequency: number, duration: number, type: OscillatorType = "sine", gainMultiplier = 1) => {
      if (!settings.soundEnabled) {
        return;
      }
      const context = ensureContext();
      if (!context) {
        return;
      }

      const oscillator = context.createOscillator();
      const gain = context.createGain();
      oscillator.type = type;
      oscillator.frequency.value = frequency;
      gain.gain.setValueAtTime(0.0001, context.currentTime);
      gain.gain.exponentialRampToValueAtTime(Math.max(0.0001, settings.volume * 0.18 * gainMultiplier), context.currentTime + 0.012);
      gain.gain.exponentialRampToValueAtTime(0.0001, context.currentTime + duration);
      oscillator.connect(gain);
      gain.connect(context.destination);
      oscillator.start();
      oscillator.stop(context.currentTime + duration + 0.04);
    },
    [ensureContext, settings.soundEnabled, settings.volume],
  );

  const play = useCallback(
    (kind: "heartbeat" | "food" | "lungs" | "liver" | "brain" | "typing" | "achievement") => {
      if (!settings.soundEnabled) {
        return;
      }
      if (kind === "heartbeat") {
        tone(68, 0.08, "sine", 0.9);
        window.setTimeout(() => tone(54, 0.07, "sine", 0.65), 105);
      }
      if (kind === "food") {
        tone(96, 0.18, "triangle", 0.6);
        window.setTimeout(() => tone(62, 0.22, "sawtooth", 0.3), 120);
      }
      if (kind === "lungs") {
        tone(420, 0.16, "sine", 0.22);
        window.setTimeout(() => tone(280, 0.28, "sine", 0.14), 90);
      }
      if (kind === "liver") {
        tone(110, 0.34, "triangle", 0.28);
      }
      if (kind === "brain") {
        tone(760, 0.09, "square", 0.16);
        window.setTimeout(() => tone(980, 0.07, "square", 0.12), 80);
      }
      if (kind === "typing") {
        tone(540, 0.025, "square", 0.08);
      }
      if (kind === "achievement") {
        tone(520, 0.09, "triangle", 0.22);
        window.setTimeout(() => tone(700, 0.11, "triangle", 0.2), 95);
      }
    },
    [settings.soundEnabled, tone],
  );

  useEffect(() => {
    if (heartbeatRef.current) {
      window.clearInterval(heartbeatRef.current);
      heartbeatRef.current = null;
    }

    if (!settings.soundEnabled || rebellionActive) {
      return undefined;
    }

    const interval =
      weather.id === "tornado" || weather.id === "thunderstorm"
        ? 620
        : weather.id === "clear"
          ? 1120
          : 860;
    heartbeatRef.current = window.setInterval(() => play("heartbeat"), interval);
    return () => {
      if (heartbeatRef.current) {
        window.clearInterval(heartbeatRef.current);
        heartbeatRef.current = null;
      }
    };
  }, [play, rebellionActive, settings.soundEnabled, weather.id]);

  return play;
}

function App() {
  const [sessionId, setSessionId] = useState(getInitialSessionId);
  const [clientName] = useState(getClientName);
  const [messages, setMessages] = useState<Message[]>([]);
  const [organs, setOrgans] = useState<Organ[]>(FALLBACK_ORGANS);
  const [sessionState, setSessionState] = useState<SessionState>(() => createInitialState(sessionId));
  const [draft, setDraft] = useState("");
  const [connected, setConnected] = useState(false);
  const [thinking, setThinking] = useState(false);
  const [activeRoundOrgans, setActiveRoundOrgans] = useState<Organ[]>([]);
  const [error, setError] = useState("");
  const [activePanel, setActivePanel] = useState<PanelTab>("status");
  const [panelsOpen, setPanelsOpen] = useState(() => !isCompactViewport());
  const [diaries, setDiaries] = useState<DiaryEntry[]>([]);
  const [diaryLoading, setDiaryLoading] = useState(false);
  const [toasts, setToasts] = useState<Toast[]>([]);
  const [pulsingMoods, setPulsingMoods] = useState<Set<string>>(new Set());
  const [copiedId, setCopiedId] = useState<Message["id"] | null>(null);
  const [sendLocked, setSendLocked] = useState(false);
  const [joinCode, setJoinCode] = useState(sessionId);
  const [apiKey, setApiKey] = useState(() => sessionStorage.getItem(API_KEY_KEY) ?? "");
  const [summary, setSummary] = useState<Summary | null>(null);
  const [summaryLoading, setSummaryLoading] = useState(false);
  const [summaryError, setSummaryError] = useState("");
  const [now, setNow] = useState(Date.now());

  const socketRef = useRef<WebSocket | null>(null);
  const bottomRef = useRef<HTMLDivElement | null>(null);
  const lastSendRef = useRef(0);
  const initialSettingsSentRef = useRef(false);

  const apiBase = useMemo(getApiBase, []);
  const wsUrl = useMemo(() => getWebSocketUrl(sessionId), [sessionId]);
  const settings = sessionState.settings;
  const weather = getVisualWeather(sessionState);
  const playSound = useSyntheticAudio(settings, weather, sessionState.rebellionActive);
  const mention = getMention(draft);
  const activeOrgans = organs.filter((organ) => settings.enabledOrgans.includes(organ.id));
  const exportHref = `${apiBase}/sessions/${encodeURIComponent(sessionId)}/export.html`;
  const shareUrl = buildShareUrl(sessionId);
  const isCrisis = weather.id === "tornado";

  const pulseMoods = useCallback((organIds: string[]) => {
    if (!organIds.length) {
      return;
    }
    setPulsingMoods((current) => new Set([...current, ...organIds]));
    window.setTimeout(() => {
      setPulsingMoods((current) => {
        const next = new Set(current);
        organIds.forEach((id) => next.delete(id));
        return next;
      });
    }, 1100);
  }, []);

  const applyState = useCallback(
    (incoming: unknown) => {
      setSessionState((current) => {
        const next = normalizeState(sessionId, incoming, current);
        const changed = ORGAN_IDS.filter((id) => Number(current.moods[id] ?? 0) !== Number(next.moods[id] ?? 0));
        pulseMoods(changed);
        return next;
      });
    },
    [pulseMoods, sessionId],
  );

  const upsertMessage = useCallback((nextMessage: Message) => {
    setMessages((current) => {
      const index = current.findIndex((message) => message.id === nextMessage.id);
      if (index === -1) {
        return [...current, nextMessage];
      }
      const next = [...current];
      next[index] = { ...next[index], ...nextMessage };
      return next;
    });
  }, []);

  const replaceStreamingMessage = useCallback((tempId: Message["id"], finalMessage: Message) => {
    setMessages((current) => {
      const index = current.findIndex((message) => message.id === tempId);
      if (index === -1) {
        return current.some((message) => message.id === finalMessage.id) ? current : [...current, finalMessage];
      }
      const next = [...current];
      next[index] = finalMessage;
      return next;
    });
  }, []);

  const pushToast = useCallback(
    (achievement: Achievement) => {
      if (!settings.achievementsEnabled) {
        return;
      }
      const toastId = `${achievement.id}-${Date.now()}`;
      setToasts((current) => [{ ...achievement, toastId }, ...current].slice(0, 4));
      playSound("achievement");
      window.setTimeout(() => {
        setToasts((current) => current.filter((toast) => toast.toastId !== toastId));
      }, 5200);
    },
    [playSound, settings.achievementsEnabled],
  );

  const sendSocket = useCallback((payload: Record<string, unknown>) => {
    const socket = socketRef.current;
    if (socket?.readyState !== WebSocket.OPEN) {
      return false;
    }
    socket.send(JSON.stringify(payload));
    return true;
  }, []);

  const patchSettings = useCallback(
    (patch: Partial<SessionSettings>) => {
      const nextSettings = normalizeSettings({ ...settings, ...patch });
      setSessionState((current) => ({
        ...current,
        settings: normalizeSettings({ ...current.settings, ...patch }),
      }));
      sendSocket({ type: "settings_update", settings: nextSettings });
    },
    [sendSocket, settings],
  );

  useEffect(() => {
    const timer = window.setInterval(() => setNow(Date.now()), 1000);
    return () => window.clearInterval(timer);
  }, []);

  useEffect(() => {
    const query = window.matchMedia("(max-width: 900px)");
    const syncPanels = () => setPanelsOpen(!query.matches);
    syncPanels();

    query.addEventListener("change", syncPanels);
    return () => query.removeEventListener("change", syncPanels);
  }, []);

  useEffect(() => {
    updateRoomUrl(sessionId);
    localStorage.setItem(SESSION_KEY, sessionId);
    setJoinCode(sessionId);
    setMessages([]);
    setDiaries([]);
    setSummary(null);
    setSummaryError("");
    setSessionState(createInitialState(sessionId));
  }, [sessionId]);

  useEffect(() => {
    let retryTimer = 0;
    let stopped = false;
    initialSettingsSentRef.current = false;

    const connect = () => {
      const socket = new WebSocket(wsUrl);
      socketRef.current = socket;

      socket.onopen = () => {
        setConnected(true);
        setError("");
        if (!initialSettingsSentRef.current && apiKey.trim()) {
          initialSettingsSentRef.current = true;
          socket.send(
            JSON.stringify({
              type: "settings_update",
              settings: { apiKey: apiKey.trim() },
            }),
          );
        }
      };

      socket.onmessage = (event) => {
        let payload: Record<string, unknown>;
        try {
          payload = JSON.parse(String(event.data)) as Record<string, unknown>;
        } catch {
          setError("Received an unreadable WebSocket payload.");
          return;
        }

        const type = String(payload.type ?? "");

        if (type === "history") {
          setMessages(Array.isArray(payload.messages) ? (payload.messages as Message[]) : []);
          setOrgans(Array.isArray(payload.organs) ? (payload.organs as Organ[]) : FALLBACK_ORGANS);
          applyState(payload.state);
          return;
        }

        if (type === "state") {
          applyState(payload.state);
          return;
        }

        if (type === "message" && payload.message) {
          upsertMessage(payload.message as Message);
          return;
        }

        if (type === "message_start" && payload.message) {
          const nextMessage = payload.message as Message;
          upsertMessage(nextMessage);
          setThinking(true);
          playSound("typing");
          if (nextMessage.senderId === "brain") playSound("brain");
          if (nextMessage.senderId === "lungs") playSound("lungs");
          if (nextMessage.senderId === "liver") playSound("liver");
          return;
        }

        if (type === "message_chunk") {
          const tempId = payload.tempId as Message["id"] | undefined;
          if (typeof tempId === "undefined") {
            return;
          }
          setMessages((current) =>
            current.map((message) =>
              message.id === tempId
                ? {
                    ...message,
                    content: `${message.content}${String(payload.chunk ?? "")}`,
                    metadata: { ...(message.metadata ?? {}), streaming: true },
                  }
                : message,
            ),
          );
          return;
        }

        if (type === "message_final" && payload.message) {
          replaceStreamingMessage(payload.tempId as Message["id"], payload.message as Message);
          return;
        }

        if (type === "organ_round_started") {
          setThinking(true);
          setActiveRoundOrgans(Array.isArray(payload.organs) ? (payload.organs as Organ[]) : []);
          return;
        }

        if (type === "organ_round_complete") {
          setThinking(false);
          setActiveRoundOrgans([]);
          return;
        }

        if (type === "achievement" && payload.achievement) {
          const achievement = payload.achievement as Achievement;
          pushToast(achievement);
          setSessionState((current) => {
            if (current.achievements.some((item) => item.id === achievement.id)) {
              return current;
            }
            return { ...current, achievements: [achievement, ...current.achievements] };
          });
          return;
        }

        if (type === "diary") {
          setDiaries(Array.isArray(payload.diaries) ? (payload.diaries as DiaryEntry[]) : []);
          setDiaryLoading(false);
          setActivePanel("diary");
          setPanelsOpen(true);
          return;
        }

        if (type === "reset") {
          setMessages(Array.isArray(payload.messages) ? (payload.messages as Message[]) : []);
          setOrgans(Array.isArray(payload.organs) ? (payload.organs as Organ[]) : FALLBACK_ORGANS);
          setDiaries([]);
          setSummary(null);
          applyState(payload.state);
          setThinking(false);
          return;
        }

        if (type === "error") {
          setError(String(payload.message ?? "Something went wrong."));
          setThinking(false);
        }
      };

      socket.onclose = () => {
        setConnected(false);
        socketRef.current = null;
        if (!stopped) {
          retryTimer = window.setTimeout(connect, 1400);
        }
      };

      socket.onerror = () => {
        setError("WebSocket connection failed.");
      };
    };

    connect();

    return () => {
      stopped = true;
      window.clearTimeout(retryTimer);
      socketRef.current?.close();
    };
  }, [apiKey, applyState, playSound, pushToast, replaceStreamingMessage, upsertMessage, wsUrl]);

  useEffect(() => {
    bottomRef.current?.scrollIntoView({ behavior: "smooth", block: "end" });
  }, [messages, thinking]);

  const sendUserMessage = useCallback(
    (content: string) => {
      const trimmed = content.trim();
      if (!trimmed) {
        return;
      }
      const nowMs = Date.now();
      if (nowMs - lastSendRef.current < SEND_DEBOUNCE_MS) {
        return;
      }
      if (!connected || !sendSocket({ type: "user_message", content: trimmed, userName: clientName, targetOrganId: getMention(trimmed)?.id })) {
        setError("WebSocket is not connected yet.");
        return;
      }

      lastSendRef.current = nowMs;
      setSendLocked(true);
      window.setTimeout(() => setSendLocked(false), SEND_DEBOUNCE_MS);

      const lower = trimmed.toLowerCase();
      if (/(burger|pizza|taco|fries|meal|ate|snack|dinner|lunch|breakfast)/.test(lower)) {
        playSound("food");
      }
      if (/(alcohol|beer|wine|vodka|whiskey|tequila|cocktail)/.test(lower)) {
        playSound("liver");
      }
      if (/(stress|anxiety|panic|spiral|can't breathe|cant breathe)/.test(lower)) {
        playSound("lungs");
      }
      setDraft("");
    },
    [clientName, connected, playSound, sendSocket],
  );

  const sendMessage = (event?: FormEvent) => {
    event?.preventDefault();
    sendUserMessage(draft);
  };

  const requestDiary = () => {
    setDiaryLoading(true);
    setActivePanel("diary");
    setPanelsOpen(true);
    if (!sendSocket({ type: "diary_request" })) {
      setDiaryLoading(false);
      setError("WebSocket is not connected yet.");
    }
  };

  const applyApiKey = () => {
    const trimmed = apiKey.trim();
    if (trimmed) {
      sessionStorage.setItem(API_KEY_KEY, trimmed);
    } else {
      sessionStorage.removeItem(API_KEY_KEY);
    }
    sendSocket({ type: "settings_update", settings: { apiKey: trimmed } });
  };

  const resetSession = () => {
    if (!window.confirm("Reset this Organiverse session?")) {
      return;
    }
    setDiaries([]);
    setSummary(null);
    setSummaryError("");
    sendSocket({ type: "reset_session" });
  };

  const joinRoom = (value: string) => {
    const next = sanitizeRoomCode(value);
    if (!next || next === sessionId) {
      return;
    }
    setSessionId(next);
  };

  const generateRoom = () => {
    const next = createRoomCode();
    setSessionId(next);
  };

  const copyText = async (value: string, copiedMessageId?: Message["id"]) => {
    try {
      await navigator.clipboard.writeText(value);
      if (copiedMessageId) {
        setCopiedId(copiedMessageId);
        window.setTimeout(() => setCopiedId(null), 1200);
      }
    } catch {
      setError("Clipboard write failed.");
    }
  };

  const requestSummary = async () => {
    setSummaryLoading(true);
    setSummaryError("");
    try {
      const response = await fetch(`${apiBase}/sessions/${encodeURIComponent(sessionId)}/summary`);
      if (!response.ok) {
        throw new Error(`HTTP ${response.status}`);
      }
      setSummary((await response.json()) as Summary);
    } catch (summaryRequestError) {
      setSummaryError(summaryRequestError instanceof Error ? summaryRequestError.message : "Summary failed.");
    } finally {
      setSummaryLoading(false);
    }
  };

  const toggleOrgan = (organId: string) => {
    const enabled = new Set(settings.enabledOrgans);
    if (enabled.has(organId)) {
      if (enabled.size === 1) {
        return;
      }
      enabled.delete(organId);
    } else {
      enabled.add(organId);
    }
    patchSettings({ enabledOrgans: ORGAN_IDS.filter((id) => enabled.has(id)) });
  };

  return (
    <>
      <div className="shell">
        <main
          className={classNames(
            "app",
            sessionState.rebellionActive && "rebellion",
            isCrisis && "crisis",
          )}
          aria-label="Organiverse chat"
        >
          <header className="topbar">
            <div className="brand">
              <span>Internal group chat</span>
              <h1>Organiverse</h1>
            </div>
            <div className="top-actions">
              <WeatherWidget weather={weather} intensity={weather.intensity} />
              <div
                className="icon-btn"
                title={wsUrl}
                style={{ display: "grid", placeItems: "center", fontSize: "0.7rem", color: connected ? "var(--accent)" : "var(--soft)", fontFamily: "var(--font-mono)" }}
              >
                {connected ? <Wifi size={16} /> : <WifiOff size={16} />}
              </div>
              <button
                className="icon-btn panel-toggle"
                onClick={() => setPanelsOpen((current) => !current)}
                aria-label="Open status panel"
                aria-expanded={panelsOpen}
                type="button"
              >
                <svg viewBox="0 0 24 24" aria-hidden="true">
                  <path d="M4 6h16M4 12h16M4 18h16" />
                </svg>
              </button>
            </div>
          </header>

          {sessionState.rebellionActive && (
            <div className="rebellion-banner" role="alert" aria-live="assertive">
              ⚠ The organs are on strike — restore order
            </div>
          )}

          <section className="status-strip" aria-label="Organ moods">
            {organs.map((organ) => (
              <OrganPill
                key={organ.id}
                organ={organ}
                mood={Number(sessionState.moods[organ.id] ?? 0)}
                active={settings.enabledOrgans.includes(organ.id)}
                pulsing={pulsingMoods.has(organ.id)}
              />
            ))}
          </section>

          <section className="chat" aria-label="Chat stream">
            <div className="day-marker">
              {sessionId} · active {formatDuration(sessionState.createdAt, now)}
            </div>
            <div className="thread">
              {messages.length === 0 ? (
                <EmptyState organs={activeOrgans.length ? activeOrgans : organs} />
              ) : (
                <>
                  {messages.map((message) => (
                    <ChatMessage
                      copied={copiedId === message.id}
                      key={message.id}
                      message={message}
                      onCopy={copyText}
                      organs={organs}
                      rebellionActive={sessionState.rebellionActive}
                    />
                  ))}
                  {thinking && <TypingIndicator organs={activeRoundOrgans.length ? activeRoundOrgans : activeOrgans} />}
                  <div ref={bottomRef} />
                </>
              )}
              {messages.length === 0 && <div ref={bottomRef} />}
            </div>
          </section>

          <footer className="composer-area">
            {error && (
              <div
                className="inline-error"
                style={{ display: "flex", gap: "8px", alignItems: "center", marginBottom: "8px", color: "#fb7185", fontSize: "0.8rem" }}
              >
                <Zap size={15} />
                <span>{error}</span>
              </div>
            )}

            <div className="quick-row">
              {QUICK_ACTIONS.map((action) => (
                <button
                  disabled={!connected || sendLocked}
                  key={action.label}
                  onClick={() => sendUserMessage(action.fill)}
                  type="button"
                >
                  {action.label}
                </button>
              ))}
            </div>

            {mention && (
              <div
                className="specialty-helper"
                style={{ display: "flex", gap: "8px", alignItems: "center", marginBottom: "8px", color: "var(--muted)", fontSize: "0.78rem" }}
              >
                <OrganAvatar organ={mention} size="tiny" rebellionActive={sessionState.rebellionActive} />
                <span>{SPECIALTY_COPY[mention.id] ?? `${mention.name} specialty mode.`}</span>
              </div>
            )}

            <form className="composer" onSubmit={sendMessage}>
              <textarea
                rows={1}
                placeholder="@Heart should I text them back?"
                autoComplete="off"
                value={draft}
                onChange={(event) => setDraft(event.target.value)}
                onKeyDown={(event) => {
                  if (event.key === "Enter" && !event.shiftKey) {
                    event.preventDefault();
                    sendMessage();
                  }
                }}
              />
              <button
                className="send"
                aria-label="Send"
                disabled={!connected || !draft.trim() || sendLocked}
                type="submit"
              >
                <svg viewBox="0 0 24 24" aria-hidden="true">
                  <path d="M22 2 11 13" />
                  <path d="m22 2-7 20-4-9-9-4Z" />
                </svg>
              </button>
            </form>
          </footer>

          <button
            className={classNames("scrim", panelsOpen && "open")}
            aria-label="Close status panel"
            onClick={() => setPanelsOpen(false)}
            type="button"
          />

          <StatusSheet
            activePanel={activePanel}
            achievements={sessionState.achievements}
            apiKey={apiKey}
            clientName={clientName}
            diaries={diaries}
            diaryLoading={diaryLoading}
            exportHref={exportHref}
            joinCode={joinCode}
            memorySummary={sessionState.memorySummary}
            moods={sessionState.moods}
            onApiKeyChange={setApiKey}
            onApiKeySave={applyApiKey}
            onClose={() => setPanelsOpen(false)}
            onCopy={copyText}
            onGenerateRoom={generateRoom}
            onJoinCodeChange={setJoinCode}
            onJoinRoom={() => joinRoom(joinCode)}
            onPanelChange={setActivePanel}
            onRequestDiary={requestDiary}
            onRequestSummary={requestSummary}
            onReset={resetSession}
            onSettingsChange={patchSettings}
            onToggleOrgan={toggleOrgan}
            open={panelsOpen}
            organs={organs}
            relationships={sessionState.relationships}
            settings={settings}
            shareUrl={shareUrl}
            summaryLoading={summaryLoading}
          />
        </main>
      </div>

      <ToastStack organs={organs} toasts={toasts} />

      {summary && (
        <SummaryModal
          onClose={() => setSummary(null)}
          onCopy={() =>
            copyText(
              `${summary.title}\n${summary.subtitle ?? ""}\n\n${summary.paragraphs
                .map((paragraph) => `${paragraph.organName}: ${paragraph.text}`)
                .join("\n\n")}`,
            )
          }
          organs={organs}
          summary={summary}
        />
      )}

      {summaryError && (
        <div
          className="floating-error"
          style={{ position: "fixed", bottom: "max(12px, env(safe-area-inset-bottom))", left: "50%", transform: "translateX(-50%)", zIndex: 60, display: "flex", gap: "8px", alignItems: "center", padding: "10px 14px", borderRadius: "10px", background: "rgba(15,21,32,0.96)", border: "1px solid rgba(244,63,94,0.3)", color: "#fb7185", fontSize: "0.8rem" }}
        >
          <Zap size={16} />
          <span>{summaryError}</span>
        </div>
      )}
    </>
  );
}

function WeatherWidget({ weather, intensity }: { weather: WeatherState; intensity?: number }) {
  return (
    <div className="weather" title={`Body weather: ${weather.label}`}>
      <WeatherSvg id={weather.id} />
      <div>
        <strong>{weather.label}</strong>
        <small>{Math.round(intensity ?? weather.intensity)}%</small>
      </div>
    </div>
  );
}

function WeatherSvg({ id }: { id: string }) {
  return (
    <svg aria-hidden="true" className="weather-svg" viewBox="0 0 64 64">
      <circle className="sun-core" cx="22" cy="22" r="10" />
      <g className="sun-rays">
        <path d="M22 4v7" />
        <path d="M22 33v7" />
        <path d="m8 9 5 5" />
        <path d="m31 32 5 5" />
        <path d="M4 22h7" />
        <path d="M33 22h7" />
      </g>
      <path className="cloud-shape" d="M21 43h25c6 0 10-4 10-9s-4-9-10-9h-2c-2-7-8-11-15-9-5 1-9 5-10 10-6 0-11 4-11 9s6 8 13 8Z" />
      <g className="rain-lines">
        <path d="m26 48-4 8" />
        <path d="m38 48-4 8" />
        <path d="m50 48-4 8" />
      </g>
      <path className="lightning-bolt" d="m34 39-6 14h8l-4 9 13-17h-8l5-6Z" />
      <path className="funnel" d="M23 39c12 3 20 3 28 0-2 7-8 12-20 18 8-1 12 1 14 4" />
      {id === "hurricane" && <circle className="hurricane-eye" cx="38" cy="38" r="7" />}
    </svg>
  );
}

function OrganPill({
  active,
  mood,
  organ,
  pulsing,
}: {
  active: boolean;
  mood: number;
  organ: Organ;
  pulsing: boolean;
}) {
  return (
    <div
      className={classNames("organ-pill", pulsing && "mood-changed")}
      style={{ "--organ": `var(--${organ.id})`, "--mood": `${Math.max(0, Math.min(100, 50 + mood / 2))}%` } as React.CSSProperties}
      data-organ={organ.id}
    >
      <OrganAvatar organ={organ} size="small" rebellionActive={false} />
      <div>
        <strong>{organ.name}</strong>
        <i />
      </div>
    </div>
  );
}

function EmptyState({ organs }: { organs: Organ[] }) {
  return (
    <div className="empty-state" style={{ display: "grid", placeItems: "center", padding: "40px 20px", textAlign: "center", color: "var(--soft)" }}>
      <div style={{ display: "flex", gap: "12px", marginBottom: "16px" }}>
        {organs.slice(0, 6).map((organ, index) => (
          <span className={`orbit orbit-${index}`} key={organ.id}>
            <OrganAvatar organ={organ} rebellionActive={false} size="small" />
          </span>
        ))}
      </div>
      <p>The organs are online.</p>
    </div>
  );
}

function ChatMessage({
  copied,
  message,
  onCopy,
  organs,
  rebellionActive,
}: {
  copied: boolean;
  message: Message;
  onCopy: (value: string, messageId?: Message["id"]) => void;
  organs: Organ[];
  rebellionActive: boolean;
}) {
  if (message.kind === "report_card") {
    return <ReportCard message={message} organs={organs} />;
  }

  const isUser = message.role === "user";
  const organ = getOrganById(organs, message.senderId);
  const isStreaming = Boolean(message.metadata?.streaming);
  const specialtyMode = Boolean(message.metadata?.specialtyMode);
  const interjection = Boolean(message.metadata?.interjection);

  return (
    <article className={classNames("msg", isUser && "user")}>
      {!isUser && <OrganAvatar organ={organ} rebellionActive={rebellionActive} size="normal" />}
      <div>
        {!isUser && (
          <div className="meta">
            <b>{message.senderName}</b>
            <time>{formatTime(message.createdAt)}</time>
          </div>
        )}
        <div
          className={classNames("bubble", isStreaming && "streaming")}
          style={
            isUser
              ? undefined
              : ({
                  "--organ": message.color || organ.color,
                } as React.CSSProperties)
          }
        >
          <p>{message.content}</p>
          {isStreaming && !message.content && <span className="typing-dot" />}
          {!isUser && message.content && (
            <button
              className="copy-bubble"
              style={{ position: "absolute", top: "-8px", right: "-8px", width: "26px", height: "26px", display: "grid", placeItems: "center", border: "none", borderRadius: "50%", background: "var(--panel-2)", color: "var(--muted)", fontSize: "0.7rem", cursor: "pointer" }}
              onClick={() => onCopy(message.content, message.id)}
              title={copied ? "Copied" : "Copy"}
              type="button"
            >
              {copied ? <Check size={13} /> : <Copy size={13} />}
            </button>
          )}
        </div>
      </div>
    </article>
  );
}

function ReportCard({ message, organs }: { message: Message; organs: Organ[] }) {
  const rows = Array.isArray(message.metadata?.rows)
    ? (message.metadata.rows as Array<{
        organId: string;
        organName: string;
        grade: string;
        domain: string;
        comment: string;
        color?: string;
      }>)
    : [];

  return (
    <article className="report">
      <div className="report-head">
        <div>
          <span>Body Report Card</span>
          <strong>{message.content || "Mixed"}</strong>
        </div>
        <b>⚠</b>
      </div>
      <div className="grades">
        {rows.map((row) => {
          const organ = getOrganById(organs, row.organId);
          return (
            <div className="grade-row" key={row.organId} style={{ "--organ": row.color ?? organ.color } as React.CSSProperties}>
              <OrganAvatar organ={organ} rebellionActive={false} size="tiny" />
              <span>
                <b>{row.organName}</b>
                <br />
                {row.comment}
              </span>
              <b>{row.grade}</b>
            </div>
          );
        })}
      </div>
    </article>
  );
}

function TypingIndicator({ organs }: { organs: Organ[] }) {
  const shown = organs.length ? organs.slice(0, 1) : FALLBACK_ORGANS.slice(0, 1);
  const organ = shown[0];
  if (!organ) return null;
  return (
    <div className="typing-indicator">
      <OrganAvatar organ={organ} rebellionActive={false} size="normal" />
      <div>
        <div className="meta">
          <b>{organ.name}</b>
          <span>typing</span>
        </div>
        <div className="typing-bubble" style={{ "--organ": `var(--${organ.id})` } as React.CSSProperties}>
          <span className="typing-dot" />
          <span className="typing-dot" />
          <span className="typing-dot" />
        </div>
      </div>
    </div>
  );
}


function StatusSheet({
  activePanel,
  achievements,
  apiKey,
  clientName,
  diaries,
  diaryLoading,
  exportHref,
  joinCode,
  memorySummary,
  moods,
  onApiKeyChange,
  onApiKeySave,
  onClose,
  onCopy,
  onGenerateRoom,
  onJoinCodeChange,
  onJoinRoom,
  onPanelChange,
  onRequestDiary,
  onRequestSummary,
  onReset,
  onSettingsChange,
  onToggleOrgan,
  open,
  organs,
  relationships,
  settings,
  shareUrl,
  summaryLoading,
}: {
  activePanel: PanelTab;
  achievements: Achievement[];
  apiKey: string;
  clientName: string;
  diaries: DiaryEntry[];
  diaryLoading: boolean;
  exportHref: string;
  joinCode: string;
  memorySummary: string;
  moods: Record<string, number>;
  onApiKeyChange: (value: string) => void;
  onApiKeySave: () => void;
  onClose: () => void;
  onCopy: (text: string) => void;
  onGenerateRoom: () => void;
  onJoinCodeChange: (value: string) => void;
  onJoinRoom: () => void;
  onPanelChange: (tab: PanelTab) => void;
  onRequestDiary: () => void;
  onRequestSummary: () => void;
  onReset: () => void;
  onSettingsChange: (patch: Partial<SessionSettings>) => void;
  onToggleOrgan: (id: string) => void;
  open: boolean;
  organs: Organ[];
  relationships: Relationship[];
  settings: SessionSettings;
  shareUrl: string;
  summaryLoading: boolean;
}) {
  return (
    <aside className={classNames("sheet", open && "open")} aria-label="Status panel" aria-modal="true">
      <div className="grabber" />
      <nav className="tabs" aria-label="Panel tabs">
        <button className={classNames(activePanel === "status" && "active")} onClick={() => onPanelChange("status")} type="button">
          <Activity size={16} />
          <span>Status</span>
        </button>
        <button className={classNames(activePanel === "diary" && "active")} onClick={() => onPanelChange("diary")} type="button">
          <BookOpen size={16} />
          <span>Diary</span>
        </button>
        <button className={classNames(activePanel === "settings" && "active")} onClick={() => onPanelChange("settings")} type="button">
          <Settings size={16} />
          <span>Settings</span>
        </button>
      </nav>

      {activePanel === "status" && (
        <div className="sheet-body">
          <section className="panel">
            <div className="section-title">
              <h2>Relationship Web</h2>
              <Users size={17} />
            </div>
            <RelationshipWeb organs={organs} relationships={relationships} />
          </section>

          <section className="panel">
            <div className="section-title">
              <h2>Mood Board</h2>
              <Activity size={17} />
            </div>
            <div className="organ-list">
              {organs.map((organ) => (
                <div className="organ-row" key={organ.id} style={{ "--organ-color": organ.color } as React.CSSProperties}>
                  <OrganAvatar organ={organ} rebellionActive={false} size="small" />
                  <div>
                    <strong>{organ.name}</strong>
                    <span>{organ.specialty}</span>
                  </div>
                  <b>{Number(moods[organ.id] ?? 0)}</b>
                </div>
              ))}
            </div>
          </section>

          <section className="panel">
            <div className="section-title">
              <h2>Memory</h2>
              <Brain size={17} />
            </div>
            <p className="memory-box">{memorySummary}</p>
          </section>

          <section className="panel">
            <div className="section-title">
              <h2>Achievements</h2>
              <Trophy size={17} />
            </div>
            <div className="achievement-list">
              {achievements.length ? (
                achievements.map((achievement) => (
                  <div className="achievement-row" key={achievement.id}>
                    <OrganAvatar organ={getOrganById(organs, achievement.organId)} rebellionActive={false} size="tiny" />
                    <div>
                      <strong>{achievement.title}</strong>
                      <span>{achievement.message}</span>
                    </div>
                  </div>
                ))
              ) : (
                <p className="muted">No achievements yet.</p>
              )}
            </div>
          </section>
        </div>
      )}

      {activePanel === "diary" && (
        <div className="sheet-body">
          <section className="panel">
            <div className="section-title">
              <h2>Organ Diary</h2>
              <BookOpen size={17} />
            </div>
            <button className="full-button" disabled={diaryLoading} onClick={onRequestDiary} type="button">
              {diaryLoading ? "Generating..." : diaries.length ? "Refresh diary view" : "Peek at diary"}
            </button>
          </section>

          <div className="diary-list">
            {diaryLoading && <p className="muted">The organs are writing privately.</p>}
            {!diaryLoading && !diaries.length && <p className="muted">Diary entries generate on demand.</p>}
            {diaries.map((diary) => (
              <article className="diary-card" key={diary.organId} style={{ "--organ-color": diary.color } as React.CSSProperties}>
                <div>
                  <OrganAvatar organ={getOrganById(organs, diary.organId)} rebellionActive={false} size="small" />
                  <h3>{diary.organName}</h3>
                </div>
                <p>{diary.entry}</p>
              </article>
            ))}
          </div>
        </div>
      )}

      {activePanel === "settings" && (
        <div className="sheet-body">
          <section className="panel">
            <div className="section-title">
              <h2>Organs</h2>
              <SlidersHorizontal size={17} />
            </div>
            <div className="toggle-grid">
              {organs.map((organ) => (
                <button
                  className={classNames("organ-toggle", settings.enabledOrgans.includes(organ.id) && "active")}
                  key={organ.id}
                  onClick={() => onToggleOrgan(organ.id)}
                  style={{ "--organ-color": organ.color } as React.CSSProperties}
                  type="button"
                >
                  <OrganAvatar organ={organ} rebellionActive={false} size="tiny" />
                  <span>{organ.name}</span>
                </button>
              ))}
            </div>
          </section>

          <section className="panel">
            <div className="section-title">
              <h2>Verbosity</h2>
              <Brain size={17} />
            </div>
            <div className="segmented">
              {(["terse", "normal", "verbose"] as Verbosity[]).map((value) => (
                <button
                  className={classNames(settings.verbosity === value && "active")}
                  key={value}
                  onClick={() => onSettingsChange({ verbosity: value })}
                  type="button"
                >
                  {value}
                </button>
              ))}
            </div>
          </section>

          <section className="panel">
            <div className="section-title">
              <h2>Sound</h2>
              {settings.soundEnabled ? <Volume2 size={17} /> : <VolumeX size={17} />}
            </div>
            <label className="switch-row">
              <span>Sound effects</span>
              <input
                checked={settings.soundEnabled}
                onChange={(event) => onSettingsChange({ soundEnabled: event.target.checked })}
                type="checkbox"
              />
            </label>
            <input
              aria-label="Volume"
              className="range"
              max="1"
              min="0"
              onChange={(event) => onSettingsChange({ volume: Number(event.target.value) })}
              step="0.05"
              type="range"
              value={settings.volume}
            />
            <label className="switch-row">
              <span>Achievements</span>
              <input
                checked={settings.achievementsEnabled}
                onChange={(event) => onSettingsChange({ achievementsEnabled: event.target.checked })}
                type="checkbox"
              />
            </label>
          </section>

          <section className="panel">
            <div className="section-title">
              <h2>DeepSeek</h2>
              <KeyRound size={17} />
            </div>
            <input
              className="text-input"
              onChange={(event) => onApiKeyChange(event.target.value)}
              placeholder="DeepSeek API key"
              type="password"
              value={apiKey}
            />
            <button className="full-button" onClick={onApiKeySave} type="button">
              Apply key
            </button>
          </section>

          <section className="panel">
            <div className="section-title">
              <h2>Multiplayer</h2>
              <Users size={17} />
            </div>
            <p className="client-name">{clientName}</p>
            <div className="join-row">
              <input className="text-input" onChange={(event) => onJoinCodeChange(event.target.value)} value={joinCode} />
              <button onClick={onJoinRoom} type="button">Join</button>
            </div>
            <div className="button-row">
              <button onClick={onGenerateRoom} type="button">New room</button>
              <button onClick={() => onCopy(shareUrl)} type="button">
                <Clipboard size={15} />
                Copy URL
              </button>
            </div>
          </section>

          <section className="panel">
            <div className="section-title">
              <h2>Export</h2>
              <Download size={17} />
            </div>
            <a className="full-button link-button" href={exportHref} rel="noreferrer" target="_blank">
              Export HTML
            </a>
            <button className="full-button" disabled={summaryLoading} onClick={onRequestSummary} type="button">
              {summaryLoading ? "Generating..." : "Session summary"}
            </button>
            <button className="danger-button" onClick={onReset} type="button">
              <RotateCcw size={16} />
              Reset session
            </button>
          </section>
        </div>
      )}
    </aside>
  );
}

function RelationshipWeb({ organs, relationships }: { organs: Organ[]; relationships: Relationship[] }) {
  const size = 360;
  const centerX = 180;
  const centerY = 158;
  const radius = 126;
  const positions = useMemo(() => {
    const map = new Map<string, { x: number; y: number }>();
    organs.forEach((organ, index) => {
      const angle = -Math.PI / 2 + (index / Math.max(1, organs.length)) * Math.PI * 2;
      map.set(organ.id, {
        x: centerX + Math.cos(angle) * radius,
        y: centerY + Math.sin(angle) * radius,
      });
    });
    return map;
  }, [organs]);

  const nodeRadius = 27;

  return (
    <svg className="relationship" viewBox={`0 0 ${size} 330`} preserveAspectRatio="xMidYMid meet" role="img">
      <defs>
        <radialGradient id="webBgGlow" cx="50%" cy="44%" r="58%">
          <stop offset="0%" stopColor="#4fd1c5" stopOpacity="0.2" />
          <stop offset="54%" stopColor="#a78bfa" stopOpacity="0.08" />
          <stop offset="100%" stopColor="#07090e" stopOpacity="0" />
        </radialGradient>
        <linearGradient id="positiveFlow" x1="0" y1="0" x2="1" y2="1">
          <stop offset="0%" stopColor="#22c55e" />
          <stop offset="100%" stopColor="#4fd1c5" />
        </linearGradient>
        <linearGradient id="negativeFlow" x1="0" y1="0" x2="1" y2="1">
          <stop offset="0%" stopColor="#fb7185" />
          <stop offset="100%" stopColor="#f59e0b" />
        </linearGradient>
        <filter id="relGlow" x="-35%" y="-35%" width="170%" height="170%">
          <feGaussianBlur stdDeviation="3" result="blur" />
          <feMerge>
            <feMergeNode in="blur" />
            <feMergeNode in="SourceGraphic" />
          </feMerge>
        </filter>
        <filter id="nodeShadow" x="-50%" y="-50%" width="200%" height="200%">
          <feDropShadow dx="0" dy="9" stdDeviation="7" floodColor="#000000" floodOpacity="0.42" />
        </filter>
      </defs>
      <rect x="0" y="0" width="360" height="330" rx="18" fill="url(#webBgGlow)" />
      <circle className="rel-orbit" cx="180" cy="158" r="126" />
      <circle className="rel-orbit" cx="180" cy="158" r="82" style={{ animationDuration: "24s", animationDirection: "reverse" }} />
      <g className="rel-links">
        {relationships.map((relationship) => {
          const source = positions.get(relationship.source);
          const target = positions.get(relationship.target);
          if (!source || !target) return null;
          const score = Math.max(-100, Math.min(100, relationship.score));
          const linkClass = score > 20 ? "positive flow" : score < -20 ? "negative flow" : "neutral thin";
          return (
            <path
              key={`${relationship.source}-${relationship.target}`}
              className={`rel-link ${linkClass}`}
              d={`M${source.x} ${source.y} C${(source.x + target.x) / 2 + (source.y - target.y) * 0.2} ${(source.y + target.y) / 2 + (target.x - source.x) * 0.2} ${(source.x + target.x) / 2 - (source.y - target.y) * 0.2} ${(source.y + target.y) / 2 - (target.x - source.x) * 0.2} ${target.x} ${target.y}`}
            />
          );
        })}
      </g>
      {organs.map((organ) => {
        const pos = positions.get(organ.id) ?? { x: centerX, y: centerY };
        return (
          <g className={`rel-node ${organ.id}`} key={organ.id} tabIndex={0} aria-label={`${organ.name}`} style={{ "--node": `var(--${organ.id})` } as React.CSSProperties}>
            <circle className="node-halo" cx={pos.x} cy={pos.y} r="38" fill={organ.color} />
            <circle className="node-core" cx={pos.x} cy={pos.y} r={nodeRadius} fill={organ.color} />
            <text className="node-icon" x={pos.x} y={pos.y}>{organ.avatar.slice(0, 1)}</text>
            <text className="node-name" x={pos.x} y={pos.y + 47}>{organ.name}</text>
          </g>
        );
      })}
    </svg>
  );
}

function ToastStack({ organs, toasts }: { organs: Organ[]; toasts: Toast[] }) {
  if (!toasts.length) {
    return null;
  }
  return (
    <div className={classNames("toast-stack", !toasts.length && "is-hidden")}>
      {toasts.map((toast) => (
        <div className="toast" role="alert" key={toast.toastId}>
          <div>
            <div className="toast-label">Achievement Unlocked</div>
            <div className="toast-title">{toast.title}</div>
            <div className="toast-body">{toast.message}</div>
          </div>
          <button className="toast-dismiss" type="button" aria-label="Dismiss">
            ×
          </button>
        </div>
      ))}
    </div>
  );
}

function SummaryModal({
  onClose,
  onCopy,
  organs,
  summary,
}: {
  onClose: () => void;
  onCopy: () => void;
  organs: Organ[];
  summary: Summary;
}) {
  return (
    <div className="modal-backdrop" role="presentation">
      <section aria-modal="true" className="summary-modal" role="dialog">
        <div className="modal-head">
          <div>
            <span>Session Summary</span>
            <h2>{summary.title}</h2>
            {summary.subtitle && <p>{summary.subtitle}</p>}
          </div>
          <button className="icon-button" onClick={onClose} type="button">
            <PanelRightClose size={18} />
          </button>
        </div>
        <div className="summary-paper">
          {summary.paragraphs.map((paragraph) => (
            <article key={paragraph.organId} style={{ "--organ-color": getOrganById(organs, paragraph.organId).color } as React.CSSProperties}>
              <OrganAvatar organ={getOrganById(organs, paragraph.organId)} rebellionActive={false} size="tiny" />
              <div>
                <h3>{paragraph.organName}</h3>
                <p>{paragraph.text}</p>
              </div>
            </article>
          ))}
        </div>
        <div className="modal-actions">
          <button onClick={onCopy} type="button">
            <Copy size={16} />
            Copy
          </button>
          <button onClick={onClose} type="button">Close</button>
        </div>
      </section>
    </div>
  );
}

function OrganAvatar({
  organ,
  rebellionActive,
  size = "normal",
}: {
  organ: Pick<Organ, "id" | "color" | "avatar" | "name">;
  rebellionActive: boolean;
  size?: "tiny" | "small" | "normal";
}) {
  return (
    <span
      className={classNames("avatar", size === "normal" && "large", rebellionActive && "avatar-inert")}
      style={{ "--organ": `var(--${organ.id})` } as React.CSSProperties}
    >
      <OrganSvg id={organ.id} />
      <span className="sr-only">{organ.name ?? organ.avatar}</span>
    </span>
  );
}

function OrganSvg({ id }: { id: string }) {
  if (id === "heart") {
    return (
      <svg viewBox="0 0 48 48">
        <path className="fill-organ heart-shape" d="M24 39S8 29 8 17c0-6 4-10 9-10 3 0 6 2 7 5 1-3 4-5 7-5 5 0 9 4 9 10 0 12-16 22-16 22Z" />
      </svg>
    );
  }
  if (id === "brain") {
    return (
      <svg viewBox="0 0 48 48">
        <path className="fill-organ" d="M22 10c-6-3-14 2-12 10-5 3-4 13 4 14 0 6 9 8 13 3V14c-1-2-3-3-5-4Z" />
        <path className="fill-organ" d="M26 10c6-3 14 2 12 10 5 3 4 13-4 14 0 6-9 8-13 3V14c1-2 3-3 5-4Z" />
        <path className="line-organ brain-arc" d="M16 21c4-2 8-1 10 2" />
        <path className="brain-arc second" d="M30 17c3 2 4 5 2 8" />
      </svg>
    );
  }
  if (id === "lungs") {
    return (
      <svg viewBox="0 0 48 48">
        <path className="line-organ" d="M24 8v14" />
        <path className="fill-organ" d="M21 21c-7 2-11 8-10 17 8 3 12-1 13-9V14" />
        <path className="fill-organ" d="M27 21c7 2 11 8 10 17-8 3-12-1-13-9V14" />
      </svg>
    );
  }
  if (id === "liver") {
    return (
      <svg viewBox="0 0 48 48">
        <path className="fill-organ liver-fill" d="M7 27c2-12 12-18 27-16 7 1 10 5 8 11-3 10-14 15-27 13-6-1-9-3-8-8Z" />
        <path className="line-organ" d="M28 15c-2 7-6 13-13 19" />
      </svg>
    );
  }
  if (id === "stomach") {
    return (
      <svg viewBox="0 0 48 48">
        <path className="fill-organ stomach-fill" d="M28 7c6 3 7 10 3 15-3 4-1 7 4 9 5 3 3 10-3 12-10 2-22-5-18-15 2-6 9-6 9-13 0-4 1-7 5-8Z" />
        <circle className="line-organ stomach-circle" cx="25" cy="29" r="6" />
      </svg>
    );
  }
  if (id === "kidneys") {
    return (
      <svg viewBox="0 0 48 48">
        <path className="fill-organ" d="M19 9c-7 1-10 9-8 17 2 9 10 13 15 8-5-4-5-9-2-15 2-5 0-9-5-10Z" />
        <path className="fill-organ" d="M29 9c7 1 10 9 8 17-2 9-10 13-15 8 5-4 5-9 2-15-2-5 0-9 5-10Z" />
        <path className="line-organ flow" d="M24 31v8" />
      </svg>
    );
  }
  return (
    <svg viewBox="0 0 48 48">
      <circle cx="24" cy="24" r="16" />
    </svg>
  );
}

export default App;
