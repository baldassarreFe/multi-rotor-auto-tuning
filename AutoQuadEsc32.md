# Identificazione dei coefficienti di un motore T-Motor mt 2212
## Strumenti
- motore
- esc
- ftdi
## Procedura di analisi
- paperozzo
## Osservazioni sull'analisi
- scelta della routine (rpm bassi, accelerazioni non brusche)
- scelta della frequenza di telemetria (limite superiore dato dall'esc)
- limitazione di decelerazione, ma non rilevante per i coefficienti
- per accelerazioni molto lente abbiamo dati scattered
## Risultati ottenuti
- come abbiamo calcolato le incertezze
- ogni tanto l'esc sbanana la telemetry, ma la procedura è in grado di scartare i valori spuri
- foto
- commento sulla valutazione delle incertezze
- l'ipotesi di corrente costante è verificata solo per il ristretto range 2000-3000 quindi i coeff che calcoliamo hanno significato fisico solo in questo range, per usare il motore in situazioni diverse, questi parametri possono non essere validi
## Possibili sviluppi
- provare altri motori e confrontare i risulati con i datasheet dei motori
- provare con lo stesso motore ed altri esc
