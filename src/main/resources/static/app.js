const responseOutput = document.querySelector("#responseOutput");
const statusBadge = document.querySelector("#statusBadge");
const tokenOutput = document.querySelector("#tokenOutput");

const savedToken = localStorage.getItem("communications.jwt");
if (savedToken) {
    tokenOutput.value = savedToken;
}

function setStatus(status, ok) {
    statusBadge.textContent = status;
    statusBadge.className = ok ? "badge ok" : "badge error";
}

function showResponse(data) {
    responseOutput.textContent = JSON.stringify(data, null, 2);
}

async function request(path, options = {}) {
    const headers = {
        ...(options.body ? {"Content-Type": "application/json"} : {}),
        ...(options.headers || {})
    };

    const response = await fetch(path, {
        method: options.method || "GET",
        headers,
        body: options.body
    });

    const text = await response.text();
    const data = text ? JSON.parse(text) : {};

    setStatus(`${response.status} ${response.statusText}`, response.ok);
    showResponse(data);

    return {response, data};
}

document.querySelector("#healthButton").addEventListener("click", async () => {
    await request("/api/health", {
        method: "GET",
        headers: {}
    });
});

document.querySelector("#registerForm").addEventListener("submit", async (event) => {
    event.preventDefault();

    await request("/api/auth/register", {
        method: "POST",
        body: JSON.stringify({
            email: document.querySelector("#registerEmail").value,
            password: document.querySelector("#registerPassword").value,
            displayName: document.querySelector("#registerDisplayName").value
        })
    });
});

document.querySelector("#loginForm").addEventListener("submit", async (event) => {
    event.preventDefault();

    const {response, data} = await request("/api/auth/login", {
        method: "POST",
        body: JSON.stringify({
            email: document.querySelector("#loginEmail").value,
            password: document.querySelector("#loginPassword").value
        })
    });

    if (response.ok && data.token) {
        tokenOutput.value = data.token;
        localStorage.setItem("communications.jwt", data.token);
    }
});

document.querySelector("#meButton").addEventListener("click", async () => {
    const token = tokenOutput.value.trim();

    await request("/api/users/me", {
        method: "GET",
        headers: token ? {Authorization: `Bearer ${token}`} : {}
    });
});

document.querySelector("#clearTokenButton").addEventListener("click", () => {
    tokenOutput.value = "";
    localStorage.removeItem("communications.jwt");
    setStatus("Token cleared", true);
    showResponse({});
});

function authorizationHeaders() {
    const token = tokenOutput.value.trim();

    if (!token) {
        return {};
    }

    return {
        Authorization: `Bearer ${token}`
    };
}

document.querySelector("#sendContactRequestForm").addEventListener("submit", async (event) => {
    event.preventDefault();

    await request("/api/contacts/requests", {
        method: "POST",
        headers: authorizationHeaders(),
        body: JSON.stringify({
            receiverEmail: document.querySelector("#receiverEmail").value
        })
    });
});

document.querySelector("#incomingRequestsButton").addEventListener("click", async () => {
    await request("/api/contacts/requests/incoming", {
        method: "GET",
        headers: authorizationHeaders()
    });
});

document.querySelector("#acceptContactRequestForm").addEventListener("submit", async (event) => {
    event.preventDefault();

    const requestId = document.querySelector("#requestId").value;

    await request(`/api/contacts/requests/${requestId}/accept`, {
        method: "POST",
        headers: authorizationHeaders()
    });
});

document.querySelector("#contactsButton").addEventListener("click", async () => {
    await request("/api/contacts", {
        method: "GET",
        headers: authorizationHeaders()
    });
});
