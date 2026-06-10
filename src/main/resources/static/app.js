const responseOutput = document.querySelector("#responseOutput");
const statusBadge = document.querySelector("#statusBadge");
const tokenOutput = document.querySelector("#tokenOutput");
const contactsList = document.querySelector("#contactsList");
const activeChatName = document.querySelector("#activeChatName");
const activeChatMeta = document.querySelector("#activeChatMeta");
const messageThread = document.querySelector("#messageThread");

let currentUser = null;
let activeContact = null;
let activeConversationId = null;

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
        currentUser = data.user;
        await loadContacts();
    }
});

document.querySelector("#meButton").addEventListener("click", async () => {
    const token = tokenOutput.value.trim();

    const {response, data} = await request("/api/users/me", {
        method: "GET",
        headers: token ? {Authorization: `Bearer ${token}`} : {}
    });

    if (response.ok) {
        currentUser = data;
    }
});

document.querySelector("#clearTokenButton").addEventListener("click", () => {
    tokenOutput.value = "";
    localStorage.removeItem("communications.jwt");
    currentUser = null;
    activeContact = null;
    activeConversationId = null;
    renderContacts([]);
    renderMessages([]);
    activeChatName.textContent = "No chat selected";
    activeChatMeta.textContent = "Choose a friend from the left.";
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
    await loadContacts();
});

document.querySelector("#refreshFriendsButton").addEventListener("click", async () => {
    await loadContacts();
});

async function loadContacts() {
    const {response, data} = await request("/api/contacts", {
        method: "GET",
        headers: authorizationHeaders()
    });

    if (response.ok) {
        renderContacts(data);
    }
}

function renderContacts(contacts) {
    if (!contacts || contacts.length === 0) {
        contactsList.innerHTML = '<p class="empty-state">No friends yet. Accept a friend request first.</p>';
        return;
    }

    contactsList.innerHTML = contacts.map(contact => `
        <button class="contact-item" type="button" data-contact-id="${contact.id}">
            <span class="contact-name">${escapeHtml(contact.displayName)}</span>
            <span class="contact-email">${escapeHtml(contact.email)}</span>
        </button>
    `).join("");

    contactsList.querySelectorAll(".contact-item").forEach(button => {
        button.addEventListener("click", async () => {
            const contact = contacts.find(item => String(item.id) === button.dataset.contactId);
            await openChat(contact);
        });
    });
}

async function openChat(contact) {
    activeContact = contact;
    activeChatName.textContent = contact.displayName;
    activeChatMeta.textContent = contact.email;
    messageThread.innerHTML = '<p class="empty-state">Loading messages...</p>';

    document.querySelectorAll(".contact-item").forEach(item => {
        item.classList.toggle("active", item.dataset.contactId === String(contact.id));
    });

    const {response, data} = await request(`/api/conversations/private/${contact.id}`, {
        method: "POST",
        headers: authorizationHeaders()
    });

    if (response.ok && data.id) {
        activeConversationId = data.id;
        await loadMessageHistory();
    }
}

document.querySelector("#messengerForm").addEventListener("submit", async (event) => {
    event.preventDefault();

    if (!activeConversationId) {
        setStatus("Choose a friend first", false);
        return;
    }

    const messageInput = document.querySelector("#messengerMessage");

    const {response} = await request(`/api/conversations/${activeConversationId}/messages`, {
        method: "POST",
        headers: authorizationHeaders(),
        body: JSON.stringify({
            content: messageInput.value
        })
    });

    if (response.ok) {
        messageInput.value = "";
        await loadMessageHistory();
    }
});

async function loadMessageHistory() {
    if (!activeConversationId) {
        renderMessages([]);
        return;
    }

    const {response, data} = await request(`/api/conversations/${activeConversationId}/messages`, {
        method: "GET",
        headers: authorizationHeaders()
    });

    if (response.ok) {
        renderMessages(data);
    }
}

function renderMessages(messages) {
    if (!messages || messages.length === 0) {
        messageThread.innerHTML = '<p class="empty-state">No messages yet. Send the first one.</p>';
        return;
    }

    messageThread.innerHTML = messages.map(message => {
        const isMine = currentUser && message.sender && message.sender.id === currentUser.id;
        const author = isMine ? "You" : message.sender.displayName;
        const time = message.sentAt ? new Date(message.sentAt).toLocaleString() : "";

        return `
            <article class="message-bubble ${isMine ? "mine" : "theirs"}">
                <div class="message-author">${escapeHtml(author)}</div>
                <div class="message-text">${escapeHtml(message.content)}</div>
                <div class="message-time">${escapeHtml(time)}</div>
            </article>
        `;
    }).join("");

    messageThread.scrollTop = messageThread.scrollHeight;
}

function escapeHtml(value) {
    return String(value)
        .replaceAll("&", "&amp;")
        .replaceAll("<", "&lt;")
        .replaceAll(">", "&gt;")
        .replaceAll('"', "&quot;")
        .replaceAll("'", "&#039;");
}
