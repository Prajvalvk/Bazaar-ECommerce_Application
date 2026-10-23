# Bazaar: storefront for the Spring Boot E-Commerce API

React 18 + Vite. Features: sign in / register, product browsing with search and categories, cart, checkout with address and payment, order tracking, and a Manage store page (add categories and products, update order status).

## Configure
Set one environment variable: `VITE_API_URL` = the public URL of your backend (for example `https://ecommerce-api.onrender.com`, no trailing slash).

## Deploy on Vercel
1. Push this folder to a GitHub repository (for example `ECommerce-Frontend`).
2. Vercel -> Add New -> Project -> import the repository (Framework: Vite, auto-detected).
3. Add the environment variable `VITE_API_URL`, then Deploy.
4. In your backend host (Render), set `CORS_ORIGINS` to your Vercel URL, for example `https://bazaar.vercel.app`, then redeploy the backend.

Build command: `npm run build` · Output directory: `dist`
