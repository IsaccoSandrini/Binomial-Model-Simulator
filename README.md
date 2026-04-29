# Options Pricing with Binomial Trees

Questo progetto Java implementa algoritmi per il calcolo del prezzo di strumenti derivati (**Opzioni Europee e Americane**) utilizzando il modello binomiale. Il sistema permette di modellizzare l'evoluzione del prezzo di un sottostante e determinare il valore equo delle opzioni attraverso la backward induction.

## 🚀 Funzionalità

* **Modellizzazione Asset**: Implementazione del modello binomiale standard e di una versione avanzata con **tassi di interesse variabili** (time-dependent parameters).
* **Pricing di Opzioni**:
    * **Opzioni Europee**: Valutazione basata sul valore atteso scontato alla maturità.
    * **Opzioni Americane**: Implementazione dell'inviluppo di Snell per gestire l'esercizio anticipato.
* **Integrazione Curve di Sconto**: Utilizzo della libreria `finmath` per l'interpolazione di curve di rendimento da prezzi di Zero Coupon Bond (ZCB).
* **Testing & Validazione**: Confronto dei risultati del modello binomiale con la formula chiusa di **Black-Scholes**.

---

## 🛠️ Architettura del Sistema

Il progetto segue i principi della programmazione a oggetti per separare il modello dell'asset dal prodotto finanziario:

### Modelli (Asset)
* **`BinomialInterface`**: Interfaccia comune che definisce i metodi per ottenere valori, probabilità e fattori di sconto.
* **`BinomialModel`**: Implementazione standard con parametri costanti ($\sigma$, $r$).
* **`BinomialModelWithTimeDependentParameters`**: Estensione che permette l'integrazione di una struttura a termine dei tassi di interesse.

### Prodotti (Opzioni)
* **`Option`**: Interfaccia per definire i metodi di pricing (`price()` e `discountedPrice()`).
* **`EuropeanOption`**: Calcola il prezzo ignorando la possibilità di esercizio anticipato.
* **`AmericanOption`**: Calcola il prezzo considerando, in ogni nodo dell'albero, il massimo tra il valore di continuazione e il payoff immediato.

---

## 📊 Esempio di Utilizzo

Il file `BinomialModelTest.java` mostra come configurare un modello e calcolare il prezzo di una Call:

```java
// Definizione parametri
double initialValue = 100;
double strike = 90;
double riskFreeRate = 0.04;
double sigma = 0.25;

// Inizializzazione modello
BinomialModel model = new BinomialModel(initialValue, riskFreeRate, sigma, 50, 5.0);

// Calcolo prezzo Opzione Americana
DoubleUnaryOperator payoff = x -> Math.max(x - strike, 0);
AmericanOption us_call = new AmericanOption(payoff, 1.0, model);
System.out.println("Prezzo call americana: " + us_call.discountedPrice()[0][0]);
```


---

## 🔬 Simulazione e Analisi Esterna
Oltre ai modelli ad albero, il progetto include test per processi stocastici continui:
* **`BrownianMotionTest`**: Generazione di incrementi del Moto Browniano e visualizzazione tramite istogrammi per validare la distribuzione normale dei rendimenti.

---

## ⚙️ Requisiti e Dipendenze
* **Java 11+**
* **Finmath Lib**: Utilizzata per le funzioni analitiche di Black-Scholes e la gestione delle curve di sconto.
* **it.univr.usefulmethodsarrays**: Libreria interna per la manipolazione di array e funzioni matematiche applicate.

---

## 📝 Note sullo Sviluppo
Il progetto evidenzia sfide tipiche del design finanziario, come la gestione di griglie temporali non uniformi e l'astrazione necessaria per estendere il pricing a modelli diversi (es. alberi trinomiali o simulazioni Monte Carlo).

---

*Sviluppato per il corso di Finanza Matematica dell'Università degli Studi di Verona
