const BASE = (import.meta.env.VITE_API_URL || '').replace(/\/$/, '');
export const configured = BASE !== '';

let token = null;
try { token = localStorage.getItem('bazaar_token'); } catch { /* ignore */ }
export const setToken = (t) => {
  token = t;
  try { t ? localStorage.setItem('bazaar_token', t) : localStorage.removeItem('bazaar_token'); } catch { /* ignore */ }
};
export const getToken = () => token;
let onAuthFail = () => {};
export const setAuthFailHandler = (f) => { onAuthFail = f; };

const getCache = new Map();
const inFlight = new Map();

function invalidateGetCache() {
  getCache.clear();
  inFlight.clear();
}

async function req(path, method = 'GET', body) {
  const headers = {};
  if (body !== undefined) headers['Content-Type'] = 'application/json';
  if (token) headers.Authorization = 'Bearer ' + token;
  let res;
  try {
    res = await fetch(BASE + path, { method, headers, body: body === undefined ? undefined : JSON.stringify(body) });
  } catch {
    throw new Error('Cannot reach the server. Check that the API is running and VITE_API_URL is correct.');
  }
  let data = null;
  try { data = await res.json(); } catch { /* empty body */ }
  if (!res.ok) {
    if (res.status === 401 && token) onAuthFail();
    const m = data && data.message;
    throw new Error(m && typeof m === 'object' ? Object.values(m).join(', ') : m || `Request failed (${res.status})`);
  }
  if (method !== 'GET') invalidateGetCache();
  return data;
}

function cachedGet(path, ttl = 30000) {
  const key = token ? token + ':' + path : path;
  const cached = getCache.get(key);
  if (cached && Date.now() - cached.time < ttl) return Promise.resolve(cached.value);
  if (inFlight.has(key)) return inFlight.get(key);
  const request = req(path).then((value) => {
    getCache.set(key, { value, time: Date.now() });
    return value;
  }).finally(() => inFlight.delete(key));
  inFlight.set(key, request);
  return request;
}

export const api = {
  get: (p) => req(p),
  cachedGet,
  post: (p, b) => req(p, 'POST', b),
  put: (p, b) => req(p, 'PUT', b),
  del: (p) => req(p, 'DELETE'),
};
