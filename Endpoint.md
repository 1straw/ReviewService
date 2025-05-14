# API-dokumentation för Produktrecensioner

## Endpoint
GET http://localhost:8080/api/v1/products/{productId}/reviews


## Grupp 4

### Full URL med parametrar

```
GET http://localhost:8080/api/v1/products/{productId}/reviews?group=group4
```

**Exempel:**

```
GET http://localhost:8080/api/v1/products/T12345/reviews?group=group4
```

### Headers

```
X-API-KEY: [GRUPP 4:s API-NYCKEL]
```

### URL-parametrar

| Parameter   | Beskrivning                                 | Obligatorisk | Standardvärde |
|-------------|----------------------------------------------|--------------|----------------|
| `productId` | ID för produkten (t.ex. "T12345")            | Ja           | –              |
| `group`     | MÅSTE vara exakt `"group4"`                  | Ja           | –              |
| `limit`     | Max antal recensioner                        | Nej          | 10             |
| `offset`    | Antal recensioner att hoppa över             | Nej          | 0              |

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

### Full URL med parametrar

```
GET http://localhost:8080/api/v1/products/{productId}/reviews?group=group5
```

**Exempel:**

```
GET http://localhost:8080/api/v1/products/T12345/reviews?group=group5
```

### Headers

```
X-API-KEY: [GRUPP 5:s API-NYCKEL]
```

### URL-parametrar

| Parameter   | Beskrivning                                 | Obligatorisk | Standardvärde |
|-------------|----------------------------------------------|--------------|----------------|
| `productId` | ID för produkten (t.ex. "T12345")            | Ja           | –              |
| `group`     | MÅSTE vara exakt `"group5"`                  | Ja           | –              |
| `limit`     | Max antal recensioner                        | Nej          | 10             |
| `offset`    | Antal recensioner att hoppa över             | Nej          | 0              |

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

### Full URL med parametrar

```
GET http://localhost:8080/api/v1/products/{productId}/reviews?group=group6
```

**Exempel:**

```
GET http://localhost:8080/api/v1/products/T12345/reviews?group=group6
```

### Headers

```
X-API-KEY: [GRUPP 6:s API-NYCKEL]
```

### URL-parametrar

| Parameter   | Beskrivning                                 | Obligatorisk | Standardvärde |
|-------------|----------------------------------------------|--------------|----------------|
| `productId` | ID för produkten (t.ex. "T12345")            | Ja           | –              |
| `group`     | MÅSTE vara exakt `"group6"`                  | Ja           | –              |
| `limit`     | Max antal recensioner                        | Nej          | 10             |
| `offset`    | Antal recensioner att hoppa över             | Nej          | 0              |

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
