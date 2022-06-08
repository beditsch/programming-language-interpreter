# CurrLang - dokumentacja końcowa (projekt TKOM)

## Temat Projektu
Język umożliwiający podstawowe przetwarzanie zmiennych zawierających wartości liczbowe z jednostkami reprezentującymi waluty.
Wsparcie typów walutowych jest zrealizowane poprzez plik konfiguracyjny, w którym użytkownik definiuje waluty oraz odpowiednie kursy wymiany.
Celem powstania języka było ułatwienie i usprawnienie działań na walutach.
## Funkcjonalności języka
- podstawowe typy danych: int, float, string, bool
- pętla while
- instrukcja warunkowa if/else
- silne, statyczne typowanie
- mutowalne zmienne
- brak automatycznej konwersji walut i innych typów, konwersje muszą być jawnie zasygnalizowane w kodzie programu
- typy danych (waluty) definiowane przez użytkownika opcjonalnie z kursami kupna i sprzedaży.
- argumenty funkcji przekazywane przez wartość

## Przykłady użycia języka
Obliczanie miesiecznego budżetu:

	int main () {
		PLN salary = 1200.0 as PLN;
		PLN tips = 15.0 as PLN;
		EUR dividend = 12.0 as EUR;
		GBP rent = -100.0 as GBP;
	
		USD my_budget = monthly_budget(salary, tips, dividend, rent, 10.0);
		print("Mam do dyspozycji " + my_budget + " w tym miesiącu.");

	}
	
	USD monthly_budget (salary, tips, dividend, rent, float savings) {
		USD budget = ((salary as USD) + (dividend as USD) + (rent as USD)) * (1 - savings);
		return budget;
	}

Obliczanie rentowności inwestycji:

	int main () {
		PLN initial_investment = 1200.0 as PLN;
		float dividend_yield = 0.012;
		int time_periods = 3;
		float growth_rate = 0.01;
	
		float roi = calculate_return(initial_investment, time_periods, dividend_yield, growth_rate);
		if (roi > 0.05) {
			print("Inwestujemy!");
		}
		else {
			print("Szukamy innych inwestycji!.");
		}
	
	}
	
	float calculate_return (initial_investment, time_periods, dividend_yield, growth_rate) {
		int i = 0;
	
		USD current_investment = initial_investment as USD;
		while (i < time_periods) {
	
			USD dividend_amount = dividend_yield * current_investment;
			current_investment = current_investment + dividend_amount;
	
			USD growth_amount = growth_rate * current_investment;
			current_investment = current_investment + growth_amount;
			i = i + 1;
		}
	
		float perc_return = (current_investment - (initial_investment as USD)) / (initial_investment as USD);
		return perc_return;
	}
	
	
Przykłady skutków wymuszania jawnej konwersji:

	int main () {
		PLN salary = 1000.0 as PLN;
		# błąd - trzeba jawnie zaznaczyć, że jest to USD
		USD dividend = 12.0;
		USD salary_usd = salary as USD;
		
		# brak jawnej konwersji walut - błąd działania
		PLN budget = salary + dividend 
		
		# błąd działania - nie można porównać innych typów
		if (salary == salary_usd) {
			do_sth();
		}
	}
	
Przykład pliku z definicjami walut i kursów:

             PLN        EUR      BITCOIN;
	PLN      1.0        0.23     0.000012
	EUR      4.55       1.0      0.000048
	BITCOIN  240000.30  50000.0  1.0

## Formalna specyfikacja i składnia
Gramatyka:
    
    program     = {function}

    function    = ("void" | type), identifier, parameters, block
    
    parameters  = "(", [[type], identifier, {",", [type], identifier}], ")"
    block       = "{", {instruction, ";" | statement}, "}"
    
    instruction         = init_instruction | assign_instruction | return_instruction | function_call | block
    init_instruction    = type, identifier, assignment
    assign_instruction  = identifier, assignment
    assignment          = "=", expression
    return_instruction  = "return", [expression]
    function_call       = identifier, arguments
    arguments           = "(", [expression, {",", expression}], ")"
    
    statement           = if_statement | while_statement
    if_statement        = "if", "(", condition, ")", instruction, ["else", instruction]
    while_statement     = "while", "(", condition, ")", block
    
    expression                  = multiplication_expression, {addition_operator, multiplication_expression}
    multiplication_expression   = factor, {multiplication_operator, factor}
    factor                      = ["-"], (function_call | "(", condition, ")" | identifier | literal), [cast_operator]
    
    condition               = and_condition, {"||", and_condition}
    and_condition           = comparison_condition, {"&&", comparison_condition}
    comparison_condition    = relational_condition, [comparison_operator, relational_condition]
    relational_condition    = base_condition, [relational_operator, base_condition]
    base_condition          = ["!"], expression
    
    identifier  = letter, {"_" | letter | digit}
	type        = "int" | "float" | "string" | "bool" | currency
	currency    = uppercase_letter, {uppercase_letter}
    
    comparison_operator     = "==" | "!="
    relational_operator     = "<" | ">" | "<=" | ">="
    addition_operator       = "+" | "-"
    multiplication_operator = "*" | "\"
    cast_operator           = "as", type
    	
    integer     = "0" | (["-"], non_zero_digit, { digit })
    float       = integer, ".", digit, {digit}
    bool        = "true" | "false"
    literal     = integer | float | string | bool
    	
	non_zero_digit      = "1" | "2" | ... | "9"
	digit               = "0" | non_zero_digit
	lowercase_letter    = "a" | "b" | ... | "z"
	uppercase_letter    = "A" | "B" | ... | "Z"
	letter              = lowercase_letter | uppercase_letter
	

## Obsługa błędów
Błędy są zgłaszane przez poszczególne komponenty interpretera i wyświetlane użytkownikowi. Komunikat o błędzie zawiera informacje przydatne do debugowania, takie jak:

- pozycja błędu w pliku wejściowym (linijka, numer znaku) - w przypadku analizatora leksykalnego oraz składniowego
- typ błędu
- komunikat błędu


## Sposób uruchomienia
Uruchomienie odbywa siępoprzez wywołanie interpretera z poziomu konsoli. Jako argumenty wejściowe do interpretera przekazać należy:

- ścieżkę do pliku z definicjami walut i kursów
- ścieżkę do pliku z kodem źródłowym

Przykładowo:
```
$ java -jar currlang.jar /Path/to/config.txt /Path/to/source/code.txt
```

## Sposób realizacji projektu
###Analizator leksykalny
Analizator leksykalny przechodzi (leniwie) przez plik tekstowy zwracając tokeny odpowiadające typowi napotkanych ciągów znakowych.

Token może być typu:
- wartości int, float, string
- operatora logicznego (operatory or, and, not, równościowe i nierównościowe)
- operatora arytmetycznego (dodawania, odejmowania, mnożenia, dzielenia)
- symbolu - np. nawiasy, średnik, przecinek
- słowa kluczowego - np. if, while, return
- identyfikatora zmiennej
- identyfikatora typu walutowego
- komentarzu
- końca tekstu


###Analizator składniowy programu
Analizator składniowy to moduł, który na podstawie otrzymywanych od leksera Tokenów buduje drzewo dokumentu programu.
Elementy składowe drzewa programu mogą być:

- Wyrażenie arytmetycznie (dodawanie, odejmowanie, mnożenie, dzielenie)
- Wyrażenie reprezentujące warunek (tzw. "and", "or", "not" oraz wszystkie warunki równościowe i nierównościowe)
- Instrukcja przypisania
- Blok instrukcji
- Definicja funkcji
- Wywołanie funkcji
- Wyrażenie warunkowe if
- Inicjacja zmiennej
- Negacja wyrażenia
- Instrukcja "return"
- Pętla while
- Identyfikator
- Literał

Analizator składniowy w pełni kontroluje poprawność ciągu Tokenów otrzymywanych od Leksera (zgodnie z gramatyką języka) i w razie rozbieżności rzuca wyjątek wraz z informacją o błędzie.

###Analizator składniowy pliku konfiguracyjnego
Analizator składniowy pliku konfiguracyjnego wykorzystuje ten sam lekser, co analizator składniowy programu.
Jego zadaniem jest parsowanie pliku z kursami wymiany walut. Wynik parsowania jest przekazywany do:
- leksera parsującego kod źródłowy, aby mógł on poprawnie rozróżnić zdefiniowane przez użytkownika typy walutowe od identyfikatorów.
- interpretera programu, aby na podstawie dostarczonych kursów wymiany walut mógł wykonywać konwersje 

###Interpreter
Zadaniem interpretera jest przejście przez drzewo programu zbudowane przez analizatora składniowego oraz wykonanie (zinterpretowanie) programu.
Interpreter jest zaimplementowany zgodnie ze wzorcem projektowym Visitor'a.
Tak, jak analizator składniowy weryfikował ciąg otrzymywanych tokenów w kontekście gramatyki języka, tak samo interpreter weryfikuje poprawność semantyczną programu.
W przypadku błędu rzuca wyjątek wraz z informacją o błędzie.


Projekt będzie się składał z trzech głównych modułów funkcjonalnych:

- analizator leksykalny - czytając znak po znaku jego zadaniem jest rozpoznawanie leksemów
- analizator składniowy - nadaje znaczenie gramatyczne tokenom otrzymywanym od analizatora leksykalnego
- analizator semantyczny - na podstawie drzewa rozbioru składniowego analizuje semantyczną poprawność instrukcji

###Użyte narzędzia
Projekt jest zaimplementowany z wykorzystaniem Kotlina. Do testów posłużyła biblioteka Kotest. Do kontroli wersji wykorzystano GitLab.

## Sposób testowania
Do zweryfikowania poprawności działania służą testy jednostkowe i integracyjne scenariuszy pozytywnych i negatywnych (łącznie 69 testów, jednak wiele z nich zawiera więcej niż jeden przypadek testowy; realnie około 200 przypadków testowych):
- leksera
- analizatora składniowego programu
- analizatora składniowego pliku konfiguracyjnego
- interpretera
- klasy pomocniczej do obsługi wyrażeń arytmetycznych
- klasy pomocniczej do obsługi walidacji typów
- klasy pomocniczej do porównań wartości
- klas modelowych, które mają dodatkowe funkcjonalności oprócz przechowywania wartości (np. Function - sprawdzenie, czy nazwy parametrów się nie powtarzają.)