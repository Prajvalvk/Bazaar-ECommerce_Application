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
  return data;
}

export const api = {
  get: (p) => req(p),
  post: (p, b) => req(p, 'POST', b),
  put: (p, b) => req(p, 'PUT', b),
  del: (p) => req(p, 'DELETE'),
};
