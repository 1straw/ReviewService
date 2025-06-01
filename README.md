# ReviewService

En REST-baserad mikroservice för att hämta produktrecensioner från olika grupper. Varje grupp har ett eget API-format och autentiseras via en unik API-nyckel.

## API-dokumentation
### Swagger 
```
http://happyreview.org/swagger-ui/index.html#
```
### Endpoint
```
GET http://happyreview.org/api/v1/group-reviews
```

### Autentisering

Alla anrop kräver en HTTP-header med en giltig API-nyckel för respektive grupp:

```
X-API-KEY: [DIN_API_NYCKEL]
```

---

## Grupp 4

**API-nyckel:**  
`ywAEEt74cNJg7_EjWiDGIzjJbLeZQ7l4cetjOq0lJzQ`

### Full URL med parametrar

```
GET http://happyreview.org/api/v1/group-reviews?group=group4
```

**Exempel:**

```
GET http://happyreview.org/api/v1/group-reviews?group=group4

curl -X 'GET' \
  'http://happyreview.org/api/v1/group-reviews?group=group4' \
  -H 'accept: application/json' \
  -H 'X-API-KEY: ywAEEt74cNJg7_EjWiDGIzjJbLeZQ7l4cetjOq0lJzQ'
```

### Headers

```
X-API-KEY: ywAEEt74cNJg7_EjWiDGIzjJbLeZQ7l4cetjOq0lJzQ
```

### URL-parametrar

| Parameter | Beskrivning                | Obligatorisk | Standardvärde |
|-----------|----------------------------|--------------|----------------|
| `group`   | MÅSTE vara exakt `"group4"` | Ja           | –              |

### Responsformat (JSON)

```json
{
  "averageRating": 4.5,
  "totalReviews": 9,
  "reviews": [
    {
      "reviewContent": "Recensionstext här...",
      "rating": 5,
      "reviewerName": "Kundnamn",
      "date": "2025-05-13"
    }
  ]
}
```

---

## Grupp 5

**API-nyckel:**  
`pD7XXZE3ZD7YPr8Jh1sxEbmYGKXKyiIUlZwhjHXx1q0`

### Full URL med parametrar

```
GET http://happyreview.org/api/v1/group-reviews?group=group5

curl -X 'GET' \
  'http://happyreview.org/api/v1/group-reviews?group=group5' \
  -H 'accept: application/json' \
  -H 'X-API-KEY: pD7XXZE3ZD7YPr8Jh1sxEbmYGKXKyiIUlZwhjHXx1q0'
```

**Exempel:**

```
GET http://happyreview.org/api/v1/group-reviews?group=group5
```

### Headers

```
X-API-KEY: pD7XXZE3ZD7YPr8Jh1sxEbmYGKXKyiIUlZwhjHXx1q0
```

### URL-parametrar

| Parameter | Beskrivning                | Obligatorisk | Standardvärde |
|-----------|----------------------------|--------------|----------------|
| `group`   | MÅSTE vara exakt `"group5"` | Ja           | –              |

### Responsformat (JSON)

```json
[
  {
    "reviewId": "abc123",
    "productId": "T12345",
    "reviewerName": "Kundnamn",
    "reviewTitle": "Recensionstitel",
    "reviewContent": "Recensionstext",
    "rating": 5,
    "creationDate": "2025-05-13"
  }
]
```

---

## Grupp 6

**API-nyckel:**  
`Hi-DlE2pzPG7XbK7HM9u8VzJuj2H6WXBJytO4a2V42o`

### Full URL med parametrar

```
GET http://happyreview.org/api/v1/group-reviews?group=group6

curl -X 'GET' \
  'http://happyreview.org/api/v1/group-reviews?group=group6' \
  -H 'accept: application/json' \
  -H 'X-API-KEY: Hi-DlE2pzPG7XbK7HM9u8VzJuj2H6WXBJytO4a2V42o'
```

**Exempel:**

```
GET http://happyreview.org/api/v1/group-reviews?group=group6
```

### Headers

```
X-API-KEY: Hi-DlE2pzPG7XbK7HM9u8VzJuj2H6WXBJytO4a2V42o
```

### URL-parametrar

| Parameter | Beskrivning                | Obligatorisk | Standardvärde |
|-----------|----------------------------|--------------|----------------|
| `group`   | MÅSTE vara exakt `"group6"` | Ja           | –              |

### Responsformat (JSON)

```json
{
  "averageRating": 4.5,
  "reviews": [
    {
      "text": "Recensionstext här...",
      "rating": 5,
      "reviewerName": "Kundnamn",
      "reviewDate": "2025-05-13"
    }
  ]
}
```

---

## Felhantering

Vid fel returneras lämplig HTTP-statuskod tillsammans med ett felmeddelande i JSON-format:

```json
{
  "error": "Unauthorized",
  "message": "Ogiltig eller saknad API-nyckel"
}
```

---

## Teknisk information

- Språk: Java 21 (Spring Boot)
- Databas: MongoDB
- Byggverktyg: Maven
- Port: 8080
- Format: JSON
- Autentisering: Header `X-API-KEY`

---

## Licens

Detta projekt är en del av ett utbildningsmoment och är inte licensierat för produktion.
