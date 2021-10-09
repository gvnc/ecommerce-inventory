// NODE_ENV
// When you run npm start, it is always equal to 'development',
// When you run npm test it is always equal to 'test'
// When you run npm run build, it is always equal to 'production'.
// You cannot override NODE_ENV manually.

const apiUrl = process.env.REACT_APP_BACKEND_URL;

export const API_URL = apiUrl;