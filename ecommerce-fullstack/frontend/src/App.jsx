import { useCallback, useEffect, useMemo, useState } from 'react';
import { api, configured, setToken, getToken, setAuthFailHandler } from './api.js';
import './styles.css';

const money = (n) => '₹' + Number(n || 0).toLocaleString('en-IN', { maximumFractionDigits: 2 });
const PAL = [
  ['#3A3AFF', '#FFD23F', '#FFFFFF'], ['#FF6B57', '#0E1330', '#FFD23F'], ['#2EC4A6', '#0E1330', '#FFFFFF'],
  ['#FFD23F', '#3A3AFF', '#0E1330'], ['#0E1330', '#FF6B57', '#FFD23F'], ['#B8B5FF', '#0E1330', '#FF6B57'],
];
const STEPS = ['ORDER_PLACED', 'PACKED', 'SHIPPED', 'DELIVERED'];
const STEP_LABEL = { ORDER_PLACED: 'Placed', PACKED: 'Packed', SHIPPED: 'Shipped', DELIVERED: 'Delivered' };
const PSTATUS = { ACTIVE: 'Active', OUT_OF_STOCK: 'Out of stock', INACTIVE: 'Inactive' };
const stepOf = (o) => o.status || 'ORDER_PLACED';

const loadUser = () => { try { return JSON.parse(localStorage.getItem('bazaar_user')); } catch { return null; } };
const saveUser = (u) => { try { u ? localStorage.setItem('bazaar_user', JSON.stringify(u)) : localStorage.removeItem('bazaar_user'); } catch { /* ignore */ } };

/* Every product gets its own colour tile, since the API has no images. */
function Tile({ p }) {
  const id = Number(p.productId) || 0;
  const [bg, fg, ink] = PAL[id % PAL.length];
  const shape = id % 4;
  const letter = (p.name || '?').trim().charAt(0).toUpperCase();
  return (
    <svg className="tile" viewBox="0 0 200 170" preserveAspectRatio="xMidYMid slice" aria-hidden="true">
      <rect width="200" height="170" fill={bg} />
      {shape === 0 && <circle cx="160" cy="40" r="62" fill={fg} />}
      {shape === 1 && [0, 1, 2, 3, 4].map((i) => <rect key={i} x={20 + i * 40} y="-10" width="18" height="200" fill={fg} transform="rotate(18 100 85)" />)}
      {shape === 2 && <path d="M0 170 A100 100 0 0 1 200 170 Z" fill={fg} />}
      {shape === 3 && <><rect x="110" y="18" width="64" height="64" fill={fg} transform="rotate(45 142 50)" /><circle cx="30" cy="40" r="14" fill={fg} /></>}
      <text x="16" y="152" fontSize="84" fontWeight="800" fill={ink} style={{ fontFamily: 'var(--display)' }}>{letter}</text>
    </svg>
  );
}

const Toast = ({ text }) => (text ? <div className="toast" role="status">{text}</div> : null);
const Field = ({ label, children }) => <label className="field"><span>{label}</span>{children}</label>;

function useEsc(fn) {
  useEffect(() => {
    const k = (e) => e.key === 'Escape' && fn();
    window.addEventListener('keydown', k);
    return () => window.removeEventListener('keydown', k);
  }, [fn]);
}

function Modal({ title, onClose, children, wide }) {
  useEsc(onClose);
  return (
    <div className="overlay" onMouseDown={(e) => e.target === e.currentTarget && onClose()}>
      <div className={'modal' + (wide ? ' wide' : '')} role="dialog" aria-modal="true" aria-label={title}>
        <button className="x" onClick={onClose} aria-label="Close">×</button>
        <h2>{title}</h2>
        {children}
      </div>
    </div>
  );
}

/* ---------- welcome + authentication ---------- */
function Welcome({ onAuth }) {
  return (
    <main>
      <section className="hero">
        <h1>Find it, add it, get it delivered.</h1>
        <p>Customers browse what the store team keeps up to date and order in a few taps. Admins manage products, their status and every order.</p>
      </section>
      <section className="wrap roles">
        <article className="role cust">
          <h2>Shop as a customer</h2>
          <p>See the available products, order the ones you want, and edit or cancel your orders while they are still being prepared.</p>
          <div className="btnrow">
            <button className="btn primary" onClick={() => onAuth('customer', 'signin')}>Customer sign in</button>
            <button className="btn ghost" onClick={() => onAuth('customer', 'signup')}>Create account</button>
          </div>
        </article>
        <article className="role adm">
          <h2>Run the store</h2>
          <p>Add and edit products, switch them between active, out of stock and inactive, and move orders from placed to delivered.</p>
          <div className="btnrow">
            <button className="btn sun" onClick={() => onAuth('admin', 'signin')}>Admin sign in</button>
            <button className="btn outline" onClick={() => onAuth('admin', 'signup')}>Admin sign up</button>
          </div>
        </article>
      </section>
    </main>
  );
}

function AuthPage({ role, mode: m0, onDone, onBack }) {
  const [mode, setMode] = useState(m0);
  const [f, setF] = useState({ name: '', email: '', password: '', adminCode: '' });
  const [err, setErr] = useState('');
  const [busy, setBusy] = useState(false);
  const admin = role === 'admin';
  const up = mode === 'signup';
  const set = (k) => (e) => setF({ ...f, [k]: e.target.value });
  async function submit(e) {
    e.preventDefault(); setErr(''); setBusy(true);
    try {
      const base = admin ? '/auth/admin' : '/auth';
      const body = up ? (admin ? f : { name: f.name, email: f.email, password: f.password }) : { email: f.email, password: f.password };
      onDone(await api.post(base + (up ? '/register' : '/login'), body));
    } catch (x) { setErr(x.message); } finally { setBusy(false); }
  }
  return (
    <main className="wrap authwrap">
      <form className={'authcard' + (admin ? ' admin' : '')} onSubmit={submit}>
        <button type="button" className="link" onClick={onBack}>Back</button>
        <h1>{admin ? (up ? 'Create an admin account' : 'Admin sign in') : (up ? 'Create your account' : 'Customer sign in')}</h1>
        {up && <Field label="Full name"><input required value={f.name} onChange={set('name')} autoComplete="name" /></Field>}
        <Field label="Email"><input required type="email" value={f.email} onChange={set('email')} autoComplete="email" /></Field>
        <Field label="Password"><input required type="password" minLength={up ? 6 : 1} value={f.password} onChange={set('password')} autoComplete={up ? 'new-password' : 'current-password'} /></Field>
        {admin && up && <Field label="Admin access code"><input required type="password" value={f.adminCode} onChange={set('adminCode')} /></Field>}
        {admin && up && <p className="muted small">Ask the store owner for the admin access code.</p>}
        {err && <p className="err" role="alert">{err}</p>}
        <button className="btn primary wide" disabled={busy}>{busy ? 'Please wait…' : up ? 'Create account' : 'Sign in'}</button>
        <p className="swap">
          {up ? 'Already have an account?' : 'New here?'}{' '}
          <button type="button" className="link" onClick={() => { setMode(up ? 'signin' : 'signup'); setErr(''); }}>{up ? 'Sign in' : (admin ? 'Create an admin account' : 'Create an account')}</button>
        </p>
      </form>
    </main>
  );
}

/* ---------- customer: cart + checkout ---------- */
function CartDrawer({ cart, setCart, onClose, say, goOrders }) {
  const [step, setStep] = useState('cart');
  const [addresses, setAddresses] = useState([]);
  const [addrId, setAddrId] = useState('');
  const [pay, setPay] = useState('UPI');
  const [newAddr, setNewAddr] = useState({ city: '', state: '', country: 'India' });
  const [showNew, setShowNew] = useState(false);
  const [err, setErr] = useState('');
  const [busy, setBusy] = useState(false);
  const [placed, setPlaced] = useState(null);
  const items = cart?.items || [];
  const total = items.reduce((s, i) => s + i.product.price * i.quantity, 0);
  useEsc(onClose);

  const change = async (item, qty) => { try { setCart(await api.put('/cart/update', { productId: item.product.productId, quantity: qty })); } catch (e) { say(e.message); } };
  const remove = async (item) => { try { setCart(await api.del('/cart/remove/' + item.product.productId)); } catch (e) { say(e.message); } };
  async function toCheckout() {
    setErr(''); setStep('checkout');
    try {
      const mine = await api.get('/addresses');
      setAddresses(mine);
      if (mine.length) setAddrId(String(mine[0].addressId)); else setShowNew(true);
    } catch (e) { setErr(e.message); }
  }
  async function saveAddress(e) {
    e.preventDefault(); setErr('');
    try {
      const a = await api.post('/addresses', newAddr);
      setAddresses([...addresses, a]); setAddrId(String(a.addressId)); setShowNew(false);
      setNewAddr({ city: '', state: '', country: 'India' });
    } catch (x) { setErr(x.message); }
  }
  async function placeOrder() {
    if (!addrId) { setErr('Choose or add a delivery address.'); return; }
    setErr(''); setBusy(true);
    try {
      const o = await api.post('/checkout', { addressId: Number(addrId), paymentMethod: pay });
      setPlaced(o); setCart({ ...cart, items: [] }); setStep('done');
    } catch (e) { setErr(e.message); } finally { setBusy(false); }
  }

  return (
    <div className="overlay drawer-wrap" onMouseDown={(e) => e.target === e.currentTarget && onClose()}>
      <aside className="drawer" role="dialog" aria-modal="true" aria-label="Your cart">
        <header className="drawer-head">
          <h2>{step === 'cart' ? 'Your cart' : step === 'checkout' ? 'Checkout' : 'Order placed'}</h2>
          <button className="x static" onClick={onClose} aria-label="Close cart">×</button>
        </header>
        {step === 'cart' && (<>
          <div className="drawer-body">
            {items.length === 0 && <div className="empty"><p><strong>Your cart is empty.</strong></p><p>Add something from the shop and it will show up here.</p></div>}
            {items.map((i) => (
              <div className="line" key={i.id}>
                <div className="thumb"><Tile p={i.product} /></div>
                <div className="line-main">
                  <strong>{i.product.name}</strong>
                  <span className="muted">{money(i.product.price)} each</span>
                  <div className="qty">
                    <button onClick={() => change(i, i.quantity - 1)} aria-label={`Decrease ${i.product.name}`}>−</button>
                    <span>{i.quantity}</span>
                    <button onClick={() => change(i, i.quantity + 1)} aria-label={`Increase ${i.product.name}`}>+</button>
                    <button className="link danger" onClick={() => remove(i)}>Remove</button>
                  </div>
                </div>
                <strong>{money(i.product.price * i.quantity)}</strong>
              </div>
            ))}
          </div>
          <footer className="drawer-foot">
            <div className="sum"><span>Total</span><strong>{money(total)}</strong></div>
            <button className="btn primary wide" disabled={!items.length} onClick={toCheckout}>Continue to checkout</button>
          </footer>
        </>)}
        {step === 'checkout' && (<>
          <div className="drawer-body stack">
            <h3>Delivery address</h3>
            {addresses.map((a) => (
              <label key={a.addressId} className={'choice' + (String(a.addressId) === addrId ? ' on' : '')}>
                <input type="radio" name="addr" checked={String(a.addressId) === addrId} onChange={() => setAddrId(String(a.addressId))} />
                <span>{a.city}, {a.state}<br /><span className="muted">{a.country}</span></span>
              </label>
            ))}
            {showNew ? (
              <form onSubmit={saveAddress} className="stack box">
                <Field label="City"><input required value={newAddr.city} onChange={(e) => setNewAddr({ ...newAddr, city: e.target.value })} /></Field>
                <Field label="State"><input required value={newAddr.state} onChange={(e) => setNewAddr({ ...newAddr, state: e.target.value })} /></Field>
                <Field label="Country"><input required value={newAddr.country} onChange={(e) => setNewAddr({ ...newAddr, country: e.target.value })} /></Field>
                <button className="btn dark">Save address</button>
              </form>
            ) : <button className="link" onClick={() => setShowNew(true)}>Add a new address</button>}
            <h3>Payment method</h3>
            <div className="pay">
              {['UPI', 'Card', 'Net banking', 'Cash on delivery'].map((m) => (
                <button key={m} type="button" className={'chip' + (pay === m ? ' on' : '')} onClick={() => setPay(m)}>{m}</button>
              ))}
            </div>
            {err && <p className="err" role="alert">{err}</p>}
          </div>
          <footer className="drawer-foot">
            <div className="sum"><span>Total</span><strong>{money(total)}</strong></div>
            <button className="btn primary wide" disabled={busy} onClick={placeOrder}>{busy ? 'Placing order…' : `Pay ${money(total)}`}</button>
            <button className="link center" onClick={() => setStep('cart')}>Back to cart</button>
          </footer>
        </>)}
        {step === 'done' && (
          <div className="drawer-body done">
            <div className="check">✓</div>
            <h3>Thank you, your order is placed.</h3>
            <p className="muted">Order {placed?.orderNo} for {money(placed?.orderAmount)}. You can edit or cancel it on the Orders page until it is packed.</p>
            <button className="btn primary wide" onClick={() => { onClose(); goOrders(); }}>View my orders</button>
          </div>
        )}
      </aside>
    </div>
  );
}

/* ---------- customer: shop ---------- */
function Shop({ products, cats, loading, slow, err, retry, addToCart, buyNow }) {
  const [q, setQ] = useState('');
  const [cat, setCat] = useState(0);
  const list = useMemo(() => products.filter((p) =>
    (!cat || p.category?.categoryId === cat) &&
    (!q || (p.name + ' ' + (p.description || '')).toLowerCase().includes(q.toLowerCase()))), [products, cat, q]);
  return (
    <main>
      <section className="hero">
        <h1>Find it, add it, get it delivered.</h1>
        <p>Pick what you need and order in one step. Products here are kept up to date by the store team.</p>
        <input className="search" type="search" placeholder="Search products" value={q} onChange={(e) => setQ(e.target.value)} aria-label="Search products" />
      </section>
      <section className="wrap">
        <div className="chips" role="tablist" aria-label="Categories">
          <button className={'chip' + (!cat ? ' on' : '')} onClick={() => setCat(0)}>All</button>
          {cats.map((c) => <button key={c.categoryId} className={'chip' + (cat === c.categoryId ? ' on' : '')} onClick={() => setCat(c.categoryId)}>{c.name}</button>)}
        </div>
        {loading && (<div><div className="grid">{[0, 1, 2, 3].map((i) => <div key={i} className="card skeleton" />)}</div>
          {slow && <p className="muted center-text">The server is waking up after a quiet period. This can take up to a minute.</p>}</div>)}
        {err && <div className="notice bad">{err} <button className="link" onClick={retry}>Try again</button></div>}
        {!loading && !err && list.length === 0 && (
          <div className="empty"><p><strong>{products.length ? 'No products match your search.' : 'No products are available yet.'}</strong></p><p>{products.length ? 'Try another word or category.' : 'Please check back soon.'}</p></div>
        )}
        <div className="grid">
          {list.map((p) => {
            const out = p.status !== 'ACTIVE';
            return (
              <article className={'card' + (out ? ' soldout' : '')} key={p.productId}>
                <div className="media"><Tile p={p} /><span className="price">{money(p.price)}</span>{out && <span className="ribbon">Out of stock</span>}</div>
                <div className="card-body">
                  <span className="muted small">{p.category?.name || 'Uncategorised'}</span>
                  <h3>{p.name}</h3>
                  <p className="desc">{p.description || 'No description yet.'}</p>
                  <div className="btnrow tight">
                    <button className="btn primary grow" disabled={out} onClick={() => buyNow(p)}>{out ? 'Out of stock' : 'Order now'}</button>
                    {!out && <button className="btn ghost" onClick={() => addToCart(p)} aria-label={`Add ${p.name} to cart`}>+ Cart</button>}
                  </div>
                </div>
              </article>
            );
          })}
        </div>
      </section>
    </main>
  );
}

/* ---------- customer: my orders (view, edit, cancel) ---------- */
function OrderEdit({ order, onClose, onSaved, say }) {
  const [qty, setQty] = useState(() => Object.fromEntries(order.items.map((i) => [i.product.productId, i.quantity])));
  const [addresses, setAddresses] = useState([]);
  const [addrId, setAddrId] = useState(order.shippingAddress ? String(order.shippingAddress.addressId) : '');
  const [err, setErr] = useState('');
  const [busy, setBusy] = useState(false);
  useEffect(() => { api.get('/addresses').then(setAddresses).catch(() => {}); }, []);
  const total = order.items.reduce((s, i) => s + i.unitPrice * (qty[i.product.productId] || 0), 0);
  async function save() {
    const items = order.items.filter((i) => qty[i.product.productId] > 0).map((i) => ({ productId: i.product.productId, quantity: qty[i.product.productId] }));
    if (!items.length) { setErr('Keep at least one product, or cancel the whole order instead.'); return; }
    setBusy(true); setErr('');
    try { await api.put('/orders/' + order.orderNo, { items, addressId: addrId ? Number(addrId) : null }); say('Order updated'); onSaved(); }
    catch (e) { setErr(e.message); } finally { setBusy(false); }
  }
  return (
    <Modal title={`Edit order ${order.orderNo}`} onClose={onClose} wide>
      <div className="stack">
        {order.items.map((i) => (
          <div className="line edit" key={i.id}>
            <div className="thumb"><Tile p={i.product} /></div>
            <div className="line-main"><strong>{i.product.name}</strong><span className="muted">{money(i.unitPrice)} each</span></div>
            <div className="qty">
              <button onClick={() => setQty({ ...qty, [i.product.productId]: Math.max(0, qty[i.product.productId] - 1) })} aria-label="Decrease">−</button>
              <span>{qty[i.product.productId]}</span>
              <button onClick={() => setQty({ ...qty, [i.product.productId]: qty[i.product.productId] + 1 })} aria-label="Increase">+</button>
            </div>
          </div>
        ))}
        <p className="muted small">Set a quantity to 0 to remove a product from this order.</p>
        <Field label="Delivery address">
          <select value={addrId} onChange={(e) => setAddrId(e.target.value)}>
            {!addrId && <option value="">Choose an address</option>}
            {addresses.map((a) => <option key={a.addressId} value={a.addressId}>{a.city}, {a.state}, {a.country}</option>)}
          </select>
        </Field>
        <div className="sum"><span>New total</span><strong>{money(total)}</strong></div>
        {err && <p className="err" role="alert">{err}</p>}
        <button className="btn primary wide" disabled={busy} onClick={save}>{busy ? 'Saving…' : 'Save changes'}</button>
      </div>
    </Modal>
  );
}

function Orders({ say }) {
  const [orders, setOrders] = useState(null);
  const [err, setErr] = useState('');
  const [edit, setEdit] = useState(null);
  const load = useCallback(async () => { try { setOrders(await api.get('/orders')); setErr(''); } catch (e) { setErr(e.message); } }, []);
  useEffect(() => { load(); }, [load]);
  async function cancel(o) {
    if (!window.confirm(`Cancel order ${o.orderNo}? This cannot be undone.`)) return;
    try { await api.del('/orders/' + o.orderNo); say('Order cancelled'); load(); } catch (e) { say(e.message); }
  }
  return (
    <main className="wrap page">
      <h1 className="page-title">Your orders</h1>
      {err && <div className="notice bad">{err}</div>}
      {!orders && !err && <p className="muted">Loading your orders…</p>}
      {orders && orders.length === 0 && <div className="empty"><p><strong>No orders yet.</strong></p><p>Choose a product in the shop to place your first order.</p></div>}
      {orders && orders.map((o) => {
        const idx = STEPS.indexOf(stepOf(o).toUpperCase());
        const open = stepOf(o) === 'ORDER_PLACED';
        return (
          <article className="order" key={o.orderNo}>
            <div className="order-head"><h3>Order {o.orderNo}</h3><span className="muted">{o.orderDate}</span><strong>{money(o.orderAmount)}</strong></div>
            <ul className="items">{o.items.map((i) => <li key={i.id}>{i.quantity} × {i.product.name} <span className="muted">{money(i.unitPrice * i.quantity)}</span></li>)}</ul>
            {o.shippingAddress && <p className="muted small">Delivering to {o.shippingAddress.city}, {o.shippingAddress.state}, {o.shippingAddress.country}</p>}
            {idx >= 0 ? <ol className="steps">{STEPS.map((s, i) => <li key={s} className={i <= idx ? 'on' : ''}><i />{STEP_LABEL[s]}</li>)}</ol> : <span className="chip on">{stepOf(o)}</span>}
            {open ? (
              <div className="btnrow tight"><button className="btn dark" onClick={() => setEdit(o)}>Edit order</button><button className="btn ghost danger" onClick={() => cancel(o)}>Cancel order</button></div>
            ) : <p className="muted small">This order is being processed, so it can no longer be changed.</p>}
          </article>
        );
      })}
      {edit && <OrderEdit order={edit} say={say} onClose={() => setEdit(null)} onSaved={() => { setEdit(null); load(); }} />}
    </main>
  );
}

/* ---------- admin ---------- */
function ProductForm({ product, cats, onClose, onSaved, say }) {
  const [p, setP] = useState({
    name: product?.name || '', price: product?.price ?? '', description: product?.description || '',
    categoryId: product?.category?.categoryId ? String(product.category.categoryId) : '', status: product?.status || 'ACTIVE',
  });
  const [err, setErr] = useState('');
  async function submit(e) {
    e.preventDefault(); setErr('');
    const body = { name: p.name, price: Number(p.price), description: p.description, status: p.status, category: p.categoryId ? { categoryId: Number(p.categoryId) } : null };
    try {
      if (product) await api.put('/products/' + product.productId, body); else await api.post('/products', body);
      say(product ? 'Product updated' : 'Product added'); onSaved();
    } catch (x) { setErr(x.message); }
  }
  return (
    <Modal title={product ? 'Edit product' : 'Add a product'} onClose={onClose}>
      <form className="stack" onSubmit={submit}>
        <Field label="Name"><input required value={p.name} onChange={(e) => setP({ ...p, name: e.target.value })} /></Field>
        <Field label="Price (₹)"><input required type="number" min="0" step="0.01" value={p.price} onChange={(e) => setP({ ...p, price: e.target.value })} /></Field>
        <Field label="Description"><textarea rows="3" value={p.description} onChange={(e) => setP({ ...p, description: e.target.value })} /></Field>
        <Field label="Category">
          <select value={p.categoryId} onChange={(e) => setP({ ...p, categoryId: e.target.value })}>
            <option value="">No category</option>
            {cats.map((c) => <option key={c.categoryId} value={c.categoryId}>{c.name}</option>)}
          </select>
        </Field>
        <Field label="Status">
          <select value={p.status} onChange={(e) => setP({ ...p, status: e.target.value })}>
            <option value="ACTIVE">Active (customers can order it)</option>
            <option value="OUT_OF_STOCK">Out of stock (visible, not orderable)</option>
            <option value="INACTIVE">Inactive (hidden from customers)</option>
          </select>
        </Field>
        {err && <p className="err" role="alert">{err}</p>}
        <button className="btn primary wide">{product ? 'Save changes' : 'Add product'}</button>
      </form>
    </Modal>
  );
}

function AdminProducts({ say }) {
  const [products, setProducts] = useState(null);
  const [cats, setCats] = useState([]);
  const [form, setForm] = useState(null);
  const [cname, setCname] = useState('');
  const [err, setErr] = useState('');
  const load = useCallback(async () => {
    try { const [p, c] = await Promise.all([api.get('/products'), api.get('/categories')]); setProducts(p); setCats(c); setErr(''); }
    catch (e) { setErr(e.message); }
  }, []);
  useEffect(() => { load(); }, [load]);
  const run = async (fn, ok) => { try { await fn(); if (ok) say(ok); load(); } catch (e) { say(e.message); } };
  return (
    <main className="wrap page">
      <div className="page-bar"><h1 className="page-title">Products</h1><button className="btn primary" onClick={() => setForm({})}>Add product</button></div>
      {err && <div className="notice bad">{err}</div>}
      <div className="box catbox">
        <strong>Categories</strong>
        <div className="chips tight">
          {cats.map((c) => <span key={c.categoryId} className="chip static">{c.name}<button aria-label={`Delete ${c.name}`} onClick={() => window.confirm(`Delete category ${c.name}?`) && run(() => api.del('/categories/' + c.categoryId), 'Category deleted')}>×</button></span>)}
          {cats.length === 0 && <span className="muted small">No categories yet.</span>}
        </div>
        <form className="inline" onSubmit={(e) => { e.preventDefault(); run(() => api.post('/categories', { name: cname }).then(() => setCname('')), 'Category added'); }}>
          <input required placeholder="New category name" value={cname} onChange={(e) => setCname(e.target.value)} aria-label="New category name" />
          <button className="btn dark">Add category</button>
        </form>
      </div>
      <div className="rows">
        {!products && !err && <p className="muted">Loading products…</p>}
        {products && products.length === 0 && <div className="empty"><p><strong>No products yet.</strong></p><p>Add your first product so customers can order it.</p></div>}
        {products && products.map((x) => (
          <div className="row" key={x.productId}>
            <div className="thumb sm"><Tile p={x} /></div>
            <div className="grow"><strong>{x.name}</strong><span className="muted small">{x.category?.name || 'Uncategorised'}</span></div>
            <strong>{money(x.price)}</strong>
            <select className={'status s-' + x.status} value={x.status} aria-label={`Status of ${x.name}`}
              onChange={(e) => run(() => api.put(`/products/${x.productId}/status`, { status: e.target.value }), 'Status updated')}>
              {Object.entries(PSTATUS).map(([k, v]) => <option key={k} value={k}>{v}</option>)}
            </select>
            <button className="link" onClick={() => setForm(x)}>Edit</button>
            <button className="link danger" onClick={() => window.confirm(`Delete ${x.name}? Products that are in orders cannot be deleted; set them to Inactive instead.`) && run(() => api.del('/products/' + x.productId), 'Product deleted')}>Delete</button>
          </div>
        ))}
      </div>
      {form && <ProductForm product={form.productId ? form : null} cats={cats} say={say} onClose={() => setForm(null)} onSaved={() => { setForm(null); load(); }} />}
    </main>
  );
}

function AdminOrders({ say }) {
  const [orders, setOrders] = useState(null);
  const [err, setErr] = useState('');
  const load = useCallback(async () => { try { setOrders(await api.get('/orders')); setErr(''); } catch (e) { setErr(e.message); } }, []);
  useEffect(() => { load(); }, [load]);
  const run = async (fn, ok) => { try { await fn(); say(ok); load(); } catch (e) { say(e.message); } };
  return (
    <main className="wrap page">
      <h1 className="page-title">Orders</h1>
      {err && <div className="notice bad">{err}</div>}
      {!orders && !err && <p className="muted">Loading orders…</p>}
      {orders && orders.length === 0 && <div className="empty"><p><strong>No orders yet.</strong></p><p>Orders placed by customers will appear here.</p></div>}
      {orders && orders.map((o) => (
        <article className="order" key={o.orderNo}>
          <div className="order-head"><h3>Order {o.orderNo}</h3><span className="muted">{o.orderDate}</span><strong>{money(o.orderAmount)}</strong></div>
          <p className="small"><strong>{o.user?.name}</strong> <span className="muted">{o.user?.email}</span></p>
          <ul className="items">{o.items.map((i) => <li key={i.id}>{i.quantity} × {i.product.name} <span className="muted">{money(i.unitPrice * i.quantity)}</span></li>)}</ul>
          {o.shippingAddress && <p className="muted small">Ship to {o.shippingAddress.city}, {o.shippingAddress.state}, {o.shippingAddress.country}</p>}
          <div className="btnrow tight">
            <label className="inline-label">Status
              <select value={stepOf(o)} onChange={(e) => run(() => api.post('/tracking', { status: e.target.value, order: { orderNo: o.orderNo } }), 'Status updated')}>
                {STEPS.map((s) => <option key={s} value={s}>{STEP_LABEL[s]}</option>)}
              </select>
            </label>
            <button className="btn ghost danger" onClick={() => window.confirm(`Delete order ${o.orderNo}?`) && run(() => api.del('/orders/' + o.orderNo), 'Order deleted')}>Delete order</button>
          </div>
        </article>
      ))}
    </main>
  );
}

function AdminUsers() {
  const [users, setUsers] = useState(null);
  const [err, setErr] = useState('');
  useEffect(() => { api.get('/admin/users').then(setUsers).catch((e) => setErr(e.message)); }, []);
  return (
    <main className="wrap page">
      <h1 className="page-title">Users</h1>
      {err && <div className="notice bad">{err}</div>}
      <div className="rows">
        {users && users.map((u) => (
          <div className="row" key={u.userId}>
            <div className="grow"><strong>{u.name}</strong><span className="muted small">{u.email}</span></div>
            <span className={'chip static' + (u.role === 'ADMIN' ? ' on' : '')}>{u.role === 'ADMIN' ? 'Admin' : 'Customer'}</span>
          </div>
        ))}
      </div>
    </main>
  );
}

/* ---------- app shell ---------- */
export default function App() {
  const [user, setUser] = useState(() => (getToken() ? loadUser() : null));
  const isAdmin = user?.role === 'ADMIN';
  const [view, setView] = useState(isAdmin ? 'a-products' : 'shop');
  const [auth, setAuth] = useState(null);
  const [products, setProducts] = useState([]);
  const [cats, setCats] = useState([]);
  const [loading, setLoading] = useState(false);
  const [slow, setSlow] = useState(false);
  const [err, setErr] = useState('');
  const [cart, setCart] = useState(null);
  const [cartOpen, setCartOpen] = useState(false);
  const [toast, setToast] = useState('');

  const say = useCallback((m) => { setToast(m); setTimeout(() => setToast(''), 3200); }, []);
  const signOut = useCallback((msg) => {
    setToken(null); saveUser(null); setUser(null); setCart(null); setCartOpen(false); setAuth(null); setView('shop');
    if (typeof msg === 'string') say(msg);
  }, [say]);
  useEffect(() => { setAuthFailHandler(() => signOut('Your session ended. Please sign in again.')); }, [signOut]);

  const loadCatalog = useCallback(async () => {
    setLoading(true); setErr('');
    const t = setTimeout(() => setSlow(true), 5000);
    try { const [p, c] = await Promise.all([api.get('/products'), api.get('/categories')]); setProducts(p); setCats(c); }
    catch (e) { setErr(e.message); }
    finally { clearTimeout(t); setSlow(false); setLoading(false); }
  }, []);
  useEffect(() => { if (user && !isAdmin) { loadCatalog(); api.get('/cart').then(setCart).catch(() => setCart({ items: [] })); } }, [user, isAdmin, loadCatalog]);
  useEffect(() => { if (user) api.get('/auth/me').catch(() => {}); }, [user]);

  const signedIn = (res) => {
    setToken(res.token); saveUser(res.user); setUser(res.user); setAuth(null);
    setView(res.user.role === 'ADMIN' ? 'a-products' : 'shop'); say(`Welcome, ${res.user.name}`);
  };
  async function addToCart(p, open) {
    try { setCart(await api.post('/cart/add', { productId: p.productId, quantity: 1 })); if (open) setCartOpen(true); else say(`${p.name} added to your cart`); }
    catch (e) { say(e.message); }
  }
  const count = (cart?.items || []).reduce((s, i) => s + i.quantity, 0);
  const NavBtn = ({ id, children }) => <button className={view === id ? 'on' : ''} onClick={() => setView(id)}>{children}</button>;

  return (
    <>
      <header className={'top' + (isAdmin ? ' admin' : '')}>
        <div className="top-in">
          <button className="logo" onClick={() => (user ? setView(isAdmin ? 'a-products' : 'shop') : setAuth(null))}><i />Bazaar{isAdmin && <small>Admin</small>}</button>
          <nav aria-label="Main">
            {user && !isAdmin && <><NavBtn id="shop">Shop</NavBtn><NavBtn id="orders">My orders</NavBtn></>}
            {isAdmin && <><NavBtn id="a-products">Products</NavBtn><NavBtn id="a-orders">Orders</NavBtn><NavBtn id="a-users">Users</NavBtn></>}
          </nav>
          <div className="actions">
            {user ? <><span className="hello">Hi, {user.name.split(' ')[0]}</span><button className="link" onClick={() => signOut()}>Sign out</button></>
              : <><button className="btn ghost" onClick={() => setAuth({ role: 'customer', mode: 'signin' })}>Sign in</button><button className="link" onClick={() => setAuth({ role: 'admin', mode: 'signin' })}>Admin</button></>}
            {user && !isAdmin && <button className="btn dark cartbtn" onClick={() => setCartOpen(true)} aria-label={`Cart, ${count} items`}>Cart <b>{count}</b></button>}
          </div>
        </div>
      </header>

      {!configured && <div className="wrap"><div className="notice" style={{ marginTop: 20 }}>This site is not connected to a backend yet. Set the <code>VITE_API_URL</code> environment variable and redeploy.</div></div>}
      {!user && !auth && <Welcome onAuth={(role, mode) => setAuth({ role, mode })} />}
      {!user && auth && <AuthPage key={auth.role + auth.mode} {...auth} onDone={signedIn} onBack={() => setAuth(null)} />}
      {user && !isAdmin && view === 'shop' && <Shop {...{ products, cats, loading, slow, err }} retry={loadCatalog} addToCart={(p) => addToCart(p, false)} buyNow={(p) => addToCart(p, true)} />}
      {user && !isAdmin && view === 'orders' && <Orders say={say} />}
      {isAdmin && view === 'a-products' && <AdminProducts say={say} />}
      {isAdmin && view === 'a-orders' && <AdminOrders say={say} />}
      {isAdmin && view === 'a-users' && <AdminUsers />}

      <footer className="foot">Bazaar is a demo store built with React and Spring Boot.</footer>
      {cartOpen && user && !isAdmin && <CartDrawer cart={cart} setCart={setCart} onClose={() => setCartOpen(false)} say={say} goOrders={() => setView('orders')} />}
      <Toast text={toast} />
    </>
  );
}
