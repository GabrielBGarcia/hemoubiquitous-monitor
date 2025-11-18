// Importar via CDN sem módulos
const script1 = document.createElement('script');
script1.src = 'https://www.gstatic.com/firebasejs/10.7.1/firebase-app-compat.js';
document.head.appendChild(script1);

const script2 = document.createElement('script');
script2.src = 'https://www.gstatic.com/firebasejs/10.7.1/firebase-messaging-compat.js';
document.head.appendChild(script2);

script2.onload = function() {
    const firebaseConfig = {
        apiKey: "AIzaSyAp8ackGIJ3c7YQB9KYCY_XjRRdr5vK-7k",
        authDomain: "hemoubiquitous-fhir.firebaseapp.com",
        projectId: "hemoubiquitous-fhir",
        storageBucket: "hemoubiquitous-fhir.firebasestorage.app",
        messagingSenderId: "375280479606",
        appId: "1:375280479606:web:0c5db7a8a5141e179c9e57",
        measurementId: "G-GR9QC2E56T"
    };

    firebase.initializeApp(firebaseConfig);
    const messaging = firebase.messaging();

    let currentToken = null;
    const BACKEND_URL = 'http://localhost:8080';

    window.requestPermission = async function() {
        try {
            const permission = await Notification.requestPermission();
            if (permission === 'granted') {
                const token = await messaging.getToken({
                    vapidKey: 'BDBRWWtson7WuBXuN6QvJGEIfgm4uEnjmdLR94IXmvTd57PUwyKb4GHlg0aJunR7Wf06HIsAQF3KSY4Yti0t99Y'
                });
                currentToken = token;
                document.getElementById('token').innerHTML = `<strong>Token:</strong><br>${token}`;
                showResult('Token obtido com sucesso!', 'success');
            } else {
                showResult('Permissão de notificação negada', 'error');
            }
        } catch (error) {
            showResult('Erro ao obter token: ' + error.message, 'error');
        }
    };

    window.subscribeToTopic = async function() {
        if (!currentToken) {
            showResult('Obtenha o token primeiro!', 'error');
            return;
        }

        const topic = document.getElementById('subscribeTopic').value;

        try {
            const response = await fetch(`${BACKEND_URL}/notifications/subscribe?token=${currentToken}&topic=${topic}`, {
                method: 'POST'
            });
            const data = await response.text();
            showResult(data, response.ok ? 'success' : 'error');
        } catch (error) {
            showResult('Erro: ' + error.message, 'error');
        }
    };

    window.sendToDevice = async function() {
        if (!currentToken) {
            showResult('Obtenha o token primeiro!', 'error');
            return;
        }

        const title = document.getElementById('deviceTitle').value;
        const body = document.getElementById('deviceBody').value;

        try {
            const response = await fetch(`${BACKEND_URL}/notifications/send-to-device`, {
                method: 'POST',
                headers: { 'Content-Type': 'application/json' },
                body: JSON.stringify({ token: currentToken, title, body })
            });
            const data = await response.text();
            showResult(data, response.ok ? 'success' : 'error');
        } catch (error) {
            showResult('Erro: ' + error.message, 'error');
        }
    };

    window.sendToTopic = async function() {
        const topic = document.getElementById('topicName').value;
        const title = document.getElementById('topicTitle').value;
        const body = document.getElementById('topicBody').value;

        try {
            const response = await fetch(`${BACKEND_URL}/notifications/send-to-topic`, {
                method: 'POST',
                headers: { 'Content-Type': 'application/json' },
                body: JSON.stringify({ topic, title, body })
            });
            const data = await response.text();
            showResult(data, response.ok ? 'success' : 'error');
        } catch (error) {
            showResult('Erro: ' + error.message, 'error');
        }
    };

    function showResult(message, type) {
        const resultDiv = document.getElementById('result');
        resultDiv.className = type;
        resultDiv.innerHTML = `<strong>${new Date().toLocaleTimeString()}:</strong> ${message}`;
    }

    messaging.onMessage((payload) => {
        console.log('Mensagem recebida:', payload);
        showResult(`Notificação recebida: ${payload.notification.title} - ${payload.notification.body}`, 'success');
    });
};
