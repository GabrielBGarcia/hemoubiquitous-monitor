// Função para carregar script dinamicamente no Service Worker
async function loadScript(url) {
    const response = await fetch(url);
    const scriptText = await response.text();
    eval(scriptText);
}

// Carregar Firebase SDK
self.addEventListener('install', (event) => {
    event.waitUntil(
        (async () => {
            await loadScript('https://www.gstatic.com/firebasejs/10.7.1/firebase-app-compat.js');
            await loadScript('https://www.gstatic.com/firebasejs/10.7.1/firebase-messaging-compat.js');

            firebase.initializeApp({
                apiKey: "AIzaSyAp8ackGIJ3c7YQB9KYCY_XjRRdr5vK-7k",
                authDomain: "hemoubiquitous-fhir.firebaseapp.com",
                projectId: "hemoubiquitous-fhir",
                storageBucket: "hemoubiquitous-fhir.firebasestorage.app",
                messagingSenderId: "375280479606",
                appId: "1:375280479606:web:0c5db7a8a5141e179c9e57",
                measurementId: "G-GR9QC2E56T"
            });

            const messaging = firebase.messaging();

            messaging.onBackgroundMessage((payload) => {
                console.log('Background Message:', payload);
                const notificationTitle = payload.notification.title;
                const notificationOptions = {
                    body: payload.notification.body,
                    icon: '/firebase-logo.png'
                };
                self.registration.showNotification(notificationTitle, notificationOptions);
            });
        })()
    );
});

self.addEventListener('activate', (event) => {
    event.waitUntil(self.clients.claim());
});
