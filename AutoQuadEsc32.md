# Identificazione dei coefficienti di un motore T-Motor MT2212

## Strumenti
- Il motore fornito per le prove sperimentali è un: T-Motor MT2212 (http://www.rctigermotor.com/html/2013/Professional_0912/44.html)
- L'ESC (electronic speed controller) utilizzato per la comunicazione con il rotore è un: AutoQuad ESC32 V2 (http://autoquad.org/wiki/wiki/aq-esc32/esc32/)
- Per la comunicazione seriale tra ESC e calcolatore, si è utilizzato una breakout board da FTDI a serial IC modello: SparkFun FTDI Basic Breakout - 3.3V (https://www.sparkfun.com/products/9873)

## Procedura di analisi
- paperozzo

## Osservazioni sull'analisi
- Come già descritto nella precedente sezione, per effettuare le procedure di calcolo dei coefficienti sul motore, è necessario avere profili di velocità crescente con accelerazione e corrente costante. Per questo motivo la routine scelta è composta da una serie di 5 accelerazioni tra 2000 e 3000 rpm, con vari profili di accelerazione costante (da 10 a 50 rpm/s), ripetute per due volte. Tra ogni accelerazione e la successiva vi è una decelerazione tra 3000 e 2000, alla massima rapidità consentita dal motore, ininfluente per il computo dei dati. Si osserva che per mantenere la corrente che scorre nel motore costante, è necessario mantenere velocità angolari e accelerazioni angolari basse.
```
accelerate: 2000 3000 10
accelerate: 3000 2000 -400
accelerate: 2000 3000 20
accelerate: 3000 2000 -400
accelerate: 2000 3000 30
accelerate: 3000 2000 -400
accelerate: 2000 3000 40
accelerate: 3000 2000 -400
accelerate: 2000 3000 50
accelerate: 3000 2000 -400

accelerate: 2000 3000 10
accelerate: 3000 2000 -400
accelerate: 2000 3000 20
accelerate: 3000 2000 -400
accelerate: 2000 3000 30
accelerate: 3000 2000 -400
accelerate: 2000 3000 40
accelerate: 3000 2000 -400
accelerate: 2000 3000 50
accelerate: 3000 2000 -400
```
- Per quanto riguarda la telemetria, si è scelto di utilizzare una frequenza di 30 Hz, che dopo alcune prove sperimentali risulta essere il limite superiore dettato dall'ESC. 
- Durante la sperimentazione e lo studio dei dati, si osserva che in questo modello di ESC vi è un limite alla decelerazione di circa -400 rpm/s. Infatti si nota che, se si impongono cambiamenti più rapidi, l'esc pone a 0V i motor volts e non ottiene la decelerazione richiesta. Per quanto riguarda l'accelerazione invece, questa sarà limitata superiormente dal valore per il quale l'esc utiliza una tensione pari a 15 V, ossia la tensione di alimentazione.
- Si osserva inoltre che i dati ottenuti durante un'accelerazione con valori molto bassi, con una frequenza di telemetria abbastanza elevata, sono significativamente scattered, ossia senza un andamento strettamente crescente come dovrebbe essere, ma con valori che oscillanti. Questo è probabilmente dettato dalla limitata accuratezza dell'analisi dell'ESC e dalla scelta implementativa della procedura di accelerazione in codice. 

## Risultati ottenuti
- come abbiamo calcolato le incertezze
- ogni tanto l'esc sbanana la telemetry, ma la procedura è in grado di scartare i valori spuri
- foto
- commento sulla valutazione delle incertezze
- l'ipotesi di corrente costante è verificata solo per il ristretto range 2000-3000 quindi i coeff che calcoliamo hanno significato fisico solo in questo range, per usare il motore in situazioni diverse, questi parametri possono non essere validi

## Possibili sviluppi
- provare altri motori e confrontare i risulati con i datasheet dei motori
- provare con lo stesso motore ed altri esc
