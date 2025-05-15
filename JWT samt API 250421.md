# Autentiseringslösning (JWT & API-nyckel)

Jag har implementerat en autentiseringslösning som stödjer både **JWT** och **API-nyckel-autentisering**.
Så vilken lösning kunden än vill ha så fungerar det med båda.

---

## Tillgängliga endpoints

### 1. Registrering

**URL:** `POST /api/auth/register`  
**Beskrivning:** Registrerar en ny användare

**Request Body:**
```json
{
  "username": "användarnamn",
  "password": "lösenord123",
  "email": "exempel@mail.com",
  "role": "USER",
  "useApiKey": true
}
```

**Noteringar:**
- `role` kan vara valfri (ingen begränsning).
- `useApiKey` (true/false) avgör om användaren får en API-nyckel.

**Svar:**
- Bekräftelsemeddelande returneras.
- Om `useApiKey` är `true`, returneras även en API-nyckel i svaret.

---

### 2. Inloggning (JWT)

**URL:** `POST /api/auth/login`  
**Beskrivning:** Loggar in användaren och returnerar en JWT-token.

**Request Body:**
```json
{
  "username": "användarnamn",
  "password": "lösenord123"
}
```

**Svar:**
```json
{
  "token": "eyJhbGciOiJIUzI1NiJ9.eyJzdWIiOiJ0ZXN0dXNlciIsIm..."
}
```

---

## Autentiseringsmetoder

### JWT-autentisering

Efter inloggning används JWT-token i `Authorization`-headern.

**Format:**
```
Authorization: Bearer <jwt-token>
```

**Exempel:**
```
Authorization: Bearer eyJhbGciOiJIUzI1NiJ9.eyJzdWIiOiJ0...
```

---

### API-nyckel-autentisering

Om användaren har registrerats med `useApiKey: true`.

**Header:**
```
X-API-KEY: <användarens-api-nyckel>
```

**Exempel:**
```
X-API-KEY: dGhpc2lzYW5leGFtcGxla2V5
```

---

## Säkerhetskonfiguration

| Endpoint               | Åtkomst                      |
|------------------------|------------------------------|
| `/api/auth/**`         | Öppen för alla (`permitAll`) |
| `/api/v1/reviews`      | Kräver autentisering         |
| Alla andra endpoints   | Kräver autentisering         |

> Det finns inga rollbaserade begränsningar i nuvarande implementation – alla autentiserade användare har tillgång till alla skyddade endpoints oavsett roll.
