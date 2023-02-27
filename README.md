# Backend Dokumentation
Her er dokumentationen for backend delen af hjemmesiden.

## Indholdsfortegnelse
1. [Cache](#cache)
2. [REST](#rest)

### Cache
- Vi har et caching lag så vi ikke henter nyt data fra API'en hver gang (den er ret langsom).
- Der afhentes `1.000` ud af de ca. `10.000` film/serier hver gang vi kontakter API'en (det er den højeste mængde pr. request).
- Når serveren starter indlæser vi alle de cachede film/serier fra en `.json` fil der ligger lokalt, hvis filen er tom venter vi på at der nedhentes ny data.
- Hele caching laget ligger på en anden tråd så serveren kan køre uafbrudt af data nedhentning.

### REST
- API'en er RESTful, så jeg har programmeret en REST klient fra bunden til at modtage data.
- Nyt data bliver hentet som råt `JSON`

<hr>

# Frontend Dokumentation
Her er dokumentationen for frontend delen af hjemmesiden.

## Indholdsfortegnelse
1. [Layout](#layout)
2. [Parametre](#parametre)

### Layout
- Vores layout er en grå hjemmeside med en variabel mængde af film/serier baseret på skærmens brede, vi har et søgefelt i toppen af siden der understøtter "full-text search" som betyder at den f.eks tjekker om "a" er noget sted i titlen på filmen.
- Når man klikker på en film/serie kommer man ind på en ny side der viser info om filmen som beskrivelsen, hvornår den kom ud, hvilke skuespillere, og hvilke instruktøre der har produceret filmen.
- Hvis man klikker på f.eks. en skuespiller viser den alle de film den skuespiller har været med i.

### Parametre
- Hjemmesiden understøtter en håndful parametre som man kan bruge til at søge efter film.
- Liste af mulige parametre:
  - Årstal (/?year=2020)
  - Genre (/?genre=drama)
  - Skuespiller (/?actor=john)
  - Instruktør (/?director=jane)
  - Søg (/?search=query)
  - Type (/?search=movie)
