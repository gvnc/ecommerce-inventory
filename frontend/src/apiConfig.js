// NODE_ENV
// When you run npm start, it is always equal to 'development',
// When you run npm test it is always equal to 'test'
// When you run npm run build, it is always equal to 'production'.
// You cannot override NODE_ENV manually.

let apiUrl = "https://" + window.location.hostname;
try {
    if(process.env.NODE_ENV === "development")
        apiUrl = "http://localhost:8080";
} catch (e) { }

export const API_URL = apiUrl;