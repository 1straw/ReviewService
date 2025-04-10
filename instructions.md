# Projekt: Väderstyrda recensioner med AI

För att kunna hjälpa CLO24:s merchbutik ska ni skapa ett projekt som hanterar produktrecensioner för kunden. I detta projekt ersätts faktiska kundkontakter med AI-genererade recensioner. Recensionerna ska anpassas efter aktuell data från ett externt API, exempelvis väderdata, men det finns även möjlighet att använda andra informationskällor.

Alla recensioner sparas i en databas. Ni väljer själva databastyp och struktur. Varje kund identifieras med en unik API-nyckel.

## Projektöversikt

Projektet går ut på att bygga ett REST API som:

1. Accepterar inloggning via API-nyckel eller användarnamn/lösenord
2. Tar emot produktinformation från kunden
3. Genererar recensioner baserat på externt API (t.ex. väder)
4. Lagrar recensioner i en databas
5. Returnerar relevanta recensioner till kunden

### Exempel på recensioner

| VaruId | Recension                                    | SkrivenAv  | Datum      | Betyg |
| ------ | -------------------------------------------- | ---------- | ---------- | ----- |
| T12345 | Skönaste t-shirten jag haft! Passar perfekt. | RockPelle  | 2025-03-28 | 5     |
| T12345 | Hade den på festival, höll hela helgen.      | HardRock   | 2024-11-04 | 4     |
| T12356 | Bästa t-shirten någonsin. Vill aldrig byta.  | Rockerbabe | 2025-03-28 | 5     |
| T12356 | Den är så skön att ha på sig.                | BikerDude  | 2024-11-04 | 5     |
| T12356 | Den var klart fulare än förväntat.           | RockPelle  | 2025-03-28 | 2     |

Notera att negativa recensioner också ska hanteras – det är viktigt för att undvika orealistiska betygsmönster.

## Autentisering och inloggning

Vid registrering anger kunden vilken typ av autentisering som ska användas:

- **API-nyckel** via `X-API-KEY` (HTTP-header)
- **Användarnamn/lösenord** via JSON-body (till `/login`)

Detta finns för att vi ska tillhandahålla flexibilitet.

Systemet svarar med ett JWT-token som används för fortsatt autentisering. Alla kunder har samma roll ("customer") och har rätt att:

- Lägga till eller ta bort produkter
- Lägga till egna recensioner
- Få recensioner genererade
- Radera sina egna recensioner

Kundens API-nyckel och eventuell produkt-API-url registreras vid kontoskapande eller senare.

### Exempel: Inloggning via användarnamn/lösenord

```http
POST /login
Content-Type: application/json
```

```json
{
  "username": "hardrockfan",
  "password": "heavymetal123"
}
```

Svar:

```json
{
  "token": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9..."
}
```

### Exempel: Inloggning via API-nyckel

```http
GET /product
X-API-KEY: abc123xyz987
```

## Produktanrop

Alla anrop går mot:

```http
POST /product
Authorization: Bearer <jwt_token>
Content-Type: application/json
```

Fältet `"mode"` avgör vilket scenario som gäller. Exempel:

### Scenario A: Endast produkt-ID

```json
{
  "mode": "productOnly",
  "productId": "T12345"
}
```

Systemet kontrollerar om produkten finns, annars skapas den, och recensioner genereras om de saknas. (detta är inte optimalt men det kan ske, då ska det skapas random data)
Ni kommer alltså att ha en kopia av kundens produktbeskrivning och produktId för att kunna generera recensioner utan att fråga kunden varje gång.

### Scenario B: Produkt-ID + extern info-URL

```json
{
  "mode": "withUrl",
  "productId": "T12345",
  "productInfoUrl": "https://kundens-api.se/products/T12345"
}
```

Systemet hämtar produktinfo från angiven URL.

### Scenario C: Direkt produktinfo

```json
{
  "mode": "withDetails",
  "productId": "T12345",
  "productName": "Whitesnake T-shirt",
  "category": "T-shirts",
  "tags": ["hårdrock", "80-tal", "svart", "bomull"]
}
```

### Scenario D: Manuell recension från kund

```json
{
  "mode": "customReview",
  "productId": "T12345",
  "review": {
    "name": "RockPelle",
    "text": "Kvaliteten var inte som förväntat",
    "rating": 2
  }
}
```

Om användaren glömt mode, utgå ifrån `productOnly` eller kolla vilka fält som skickats in för att avgöra. (överkurs)

## Recensionslogik och externa API:er

Recensioner genereras via AI baserat på produktdata och extern API-data. Väderdata är vanligast (OpenWeatherMap, WeatherAPI), men det är **inte ett krav**. Andra exempel:

- Ett **kattfakta-API** som gör recensionerna lekfulla
- Ett **evenemangs-API** som lägger in festival- eller kontextreferenser
- Ett **helgdagars-API** som refererar till aktuella helgdagar

Viktigt är att API-datan bidrar med variation och relevans.

### Principer:

- Framtidsdatum får inte användas
- Om API-data saknas för visst datum: använd standardvärden ("soligt", etc.) eller slumpa.
- Minst 5 recensioner per produkt ska alltid finnas
- Endast de senaste 2 månadernas recensioner visas
- Max 10 recensioner returneras (inställningsbart)
- Om färre än 5 nya recensioner: generera fler
- Om färre än 5 recensioner finns för de senaste 2 månaderna: generera fler

### Statistik (exempel):

```json
{
  "currentAverage": 4.3, // genomsnittligt betyg för de senaste 2 månaderna
  "allTimeAverage": 4.7, // genomsnittligt betyg för alla recensioner
  "totalReviews": 8, // totalt antal recensioner för de senaste 2 månaderna
  "totalReviewsAllTime": 50, // totalt antal recensioner genom tiderna
  "lastReviewDate": "2025-03-28", // senaste datum för recension
  "lastReviewRating": 5, // senaste betyg
  "median": 5, // medianbetyg för de senaste 2 månaderna
  "medianAllTime": 4.5, // medianbetyg för alla recensioner
  "mode": 5, // mest frekventa betyg för de senaste 2 månaderna
  "modeAllTime": 4 // mest frekventa betyg för alla recensioner
}
```

Observera att troligen så är kunden bara intresserad av currentAverage, totalReviews och lastReviewDate.
Det är viktigt att ni inte överöser kunden med data som de inte är intresserade av.
Kolla med kunden först vad de vill ha.

## Databasstruktur

Databasen ska innehålla:

- Kundinformation (API-nycklar, inloggningsdata)
- Produktinformation kopplad till kund
- Recensioner kopplade till produkter
- (Valfritt) AI-loggar med prompt/svar

## Exempelsvar från `/product`

```json
{
  "productId": "T12345",
  "productName": "Whitesnake T-shirt",
  "averageRating": 4.5,
  "reviews": [
    {
      "date": "2025-03-28",
      "name": "RockPelle87",
      "rating": 5,
      "text": "Sitter som en smäck och står emot västkustregn."
    },
    {
      "date": "2024-11-04",
      "name": "HardRockLisa",
      "rating": 4,
      "text": "Skön även när det snöar. Kunde haft längre ärmar."
    }
  ]
}
```

## Rekommenderat arbetsflöde

### Steg 1: Skapa projekt

1. Skapa nytt Spring Boot-projekt (eller annat Java-ramverk)
2. Lägg till beroenden:
   - Web/REST
   - JDBC/JPA
   - JWT-hantering
   - HTTP-klient

### Steg 2: Databas

1. Skapa schema
2. Konfigurera anslutning
3. Skapa entiteter

### Steg 3: API-utveckling

1. Skapa controllers
2. Implementera autentisering
3. Implementera produktlogik

### Steg 4: Integrationer

1. Registrera externt API (väder eller liknande)
2. Implementera hämtning av data
3. Anropa AI-tjänst för recensioner

### Steg 5: Testa

1. Testa varje endpoint
2. Kontrollera att recensioner genereras korrekt
3. Verifiera hur externt API påverkar recensionerna

## Grupparbete och bedömning

- Arbeta i grupper om 2–4 personer
- Använd GitHub eller liknande för koddelning
- Arbeta agilt med dagliga avstämningar
- Strukturera koden gemensamt, med README och tydlig mappstruktur

Varje deltagare ska:

- Skriva en **egen reflektion** med:
  - Vad du lärt dig
  - Hur du bidrog
  - Vad som fungerade / kunde gjorts bättre

**Bedömning sker individuellt** baserat på:

- Kodkvalitet
- Funktionalitet
- Hur väl projektbeskrivningen följts
- Din reflektion

## Deadline

Bestäms av gruppen tillsammans med projektledaren.
Men senast vecka 22. Tänk på att bli klara innan så ni hinner testa och
testa mot kunderna. Det är viktigt att ni har en fungerande version av projektet vid deadline.
Om ni inte hinner klart, se till att ha en fungerande del av projektet som kan demonstreras.