# Identificazione dei coefficienti di un motore T-Motor mt 2212

## Strumenti
- motore
- esc
- ftdi

## Procedura di analisi
La procedura di analisi utilizzata si basa sui lavori svolti da Bangura et al. [1] per l'identificazione del modello dei rotori di un quadricottero. Ai fini dell'identificazione è necessario stimare per ogni rotore i coefficienti caratteristici presenti in questa equazione:  
![v_a = K_e\omega + R_a i_a + L_a \frac{di_a}{dt}](/assets/eq1.gif)  
in cui Ke è la costante delle armature del motore proporzionale al campo elettromagnetico generato, Ra è la restistenza del motore e La la sua induttanza. Al fine di semplificare la procedura per l'identificazione di questi paramtetri si considera il motore in *steady state*, operante cioè in condizioni di corrente costante, in questo modo la derivata della corrente è nulla ed La si semplifica dall'equazione. Nelle suddette condizioni il motore opera erogando una coppia e quindi un'accelerazione angolare costanti, legate tra loro dalla seguente relazione, in cui si introduce anche il parametro Kq, costante di coppia del rotore:  
![\tau = K_q i_a = I \dot{\omega}](/assets/eq2.gif)  
Tramite l'ESC è possibile eseguire una routine ad accelerazione, ottenendo istante per istante una stima della corrente circolante nel motore, della tensione applicata e degli rpm.  
Tramite regressione lineare si valuta l'accelerazione angolare del rotore, Unitamente al momento di interzia di un disco applicato al motore, l'accelerazione è usata per stimare la coppia generata. Successivamente si stima la corrente media, supposta costante e la si usa per calcolare infine il parametro Kq.
![K_q = \frac{\tau}{\overline{i_a}} = \frac{I \dot{\omega}}{\overline{i_a}}](/assets/eq3.gif)  
Inoltre, tramite regressione lineare tra gli rpm e la tensione del motore si ottengono Ke e Ra, rispettivamente come coefficiente angolare e ordinata all'origine divisa per la corrente media:  
![v_a = K_e\omega + R_a i_a](/assets/eq4.gif)  
L'analisi qui descritta è condotta tramite un software appositamente sviluppato per gestire la comunicazione tramite seriale con un motore collegato a un ESC, la raccolta dei dati della telemetria e la loro analisi.

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


[1] M. Bangura, H. Lim, H. Kim, and R. Mahony, "Aerodynamic power control for multirotor aerial vehicles", in Robotics and Automation (ICRA), 2014 IEEE International Conference on, May 2014, pp 529-536
