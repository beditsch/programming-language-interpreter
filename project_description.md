# TKOM - projekt - opis

## Temat Projektu
Język umożliwiający podstawowe przetwarzanie zmiennych zawierających wartości liczbowe z jednostkami reprezentującymi waluty. Powinna istnieć możliwość deklaratywnego zdefiniowania relacji pomiędzy walutami (przelicznika), która będzie wykorzystana przy operacji np. dodawania wartości o dwóch różnych walutach.

## Funkcjonalności języka
- podstawowe typy danych: int, float, string, bool, 
- pętla while
- instrukcja warunkowa if/else
- silne, statyczne typowanie
- brak automatycznej konwersji walut, konwersje muszą być jawnie zasygnalizowane w kodzie programu
- typy danych (waluty) definiowane przez użytkownika wraz z kursami w stosunku do waluty głównej (z kursem 1.0)
- kurs wymiany walut nie zależy od rodzaju transakcji (kupno/sprzedaż)

## Przykłady użycia języka
Obliczanie miesiecznego budżetu:

	int main () {
		PLN salary = 1200.0;
		PLN tips = 15.0;
		EUR dividend = 12.0;
		GBP rent = -100.0;
	
		USD my_budget = monthly_budget(salary, tips, dividend, rent);
		print("Mam do dyspozycji " + my_budget + typeof(my_budget) + " w tym miesiącu.");
	
	}
	
	USD monthly_budget (salary, tips, dividend, rent, float savings) {
		USD budget = ((USD) salary + (USD) dividend + (USD) rent) * (1 - savings);
		return budget;
	}

Obliczanie rentowności inwestycji:

	int main () {
		PLN initial_innvestment = 1200.0;
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
	
		USD current_investment = (USD) initial_investment;
		while (i < time_periods) {
	
			USD dividend_amount = dividend_yield * current_investment;
			current_investment = current_investment + dividend_amount;
	
			USD growth_amount = growth_rate * current_investment;
			current_investment = current_investment + growth_amount;
			i += 1;
		}
	
		perc_return = (current_investment - (USD) initial_investment) / initial_investment;
		return perc_return;
	}
	
	
Przykłady skutków wymuszania jawnej konwersji:

	int main () {
		PLN salary = 1000.0;
		USD dividend = 12.0;
		USD salary_usd = (PLN) salary;
		
		# brak jawnej konwersji walut - błąd działania
		PLN budget = salary + divident 
		
		if (salary == salary_usd) {
			# This block will not be executed, the currencies are not the same
			do_sth();
		}
	}
	
Przykład pliku z definicjami walut i kursów:

	PLN 1.0
	EUR 4.62
	USD 3.97
	BITCOIN 240000.30
	DOGECOIN = 10.4

## Formalna specyfikacja i składnia
Gramatyka:

	non_zero_digit = "1" | "2" | ... | "9"
	digit = "0" | non_zero_digit
	lowercase_letter = "a" | "b" | ... | "z"
	uppercase_letter = "A" | "B" | ... | "Z"
	letter = lowercase_letter | uppercase_letter
	
	integer =  "0" | (["-"], non_zero_digit, { digit })
	float =  integer, ".", digit, {digit}
	bool = "true" | "false"
	literal = integer | float | string | bool
	
	comparison_operator = "==" | "!="
	relational_operator = "<" | ">" | "<=" | ">="
	addition_operator = "+" | "-"
	multiplication_operator = "*" | "\"
	cast_operator = "(", type, ")"
	
	identifier = letter, {"_" | letter | digit}
	type = "int" | "float" | "string" | "bool" | currency
	currency = uppercase_letter, {uppercase_letter}
	
	base_condition = ["!"], [cast_operator], expression
	relational_condition = condition_base, [relational_operator, base_condition]
	comparison_condition = relational_condition, [comparison_operator, relational_condition]
	and_condition = comparison_condition, {"&&", comparison_condition}
	relational_condition = base_condition, [relational_operator, base_condition]
	condition = and_condition, {"||", and_condition}
	
	expression = multiplication_expression, {addition_operator, multiplication_expression}
	multiplication_expression = factor, {multiplication_operator, factor}
	factor = function_call | "(", expression, ")" | identifier | literal)

    instruction = init_instruction | assign_instruction | return_instruction | function_call
    init_instruction = type, identifier, [assignment]
    assign_instruction	= identifier, assignment
    assignment = "=", expression
    return_instruction 	= "return", expression
    function_call = identifier, arguments
    arguments = "(", [expression, {",", expression}], ")"

	 statement  = if_statement | while_statement
    if_statement = "if", "(", condition, ")", block, ["else", block]
    while_statement = "while", "(", condition, ")", block

    block = "{", {instruction, ";" | statement}, "}"
    parameters = "(", [[type], identifier, {",", [type], identifier}], ")"
    
    function 		= ("void" | type), identifier, parameters, block
   
    program 		= {function}

## Obsługa błędów
Błędy będą zgłaszane przez poszczególne komponenty interpretera i wyświetlane użytkownikowi. Komunikat o błędzie zawierać będzie informacje przydatne do debugowania, takie jak:

- pozycja błędu w pliku wejściowym (linijka, numer znaku)
- typ błędu
- komunikat błędu


## Sposób uruchomienia
Uruchomienie będzie się odbywało poprzez wywołanie interpretera z poziomu konsoli. Jako argumenty wejściowe do interpretera przekazane zostaną:

- ścieżka do pliku z kodem źródłowym
- ścieżka do pliku z definicjami walut i kursów

## Sposób realizacji projektu
Projekt będzie się składał z trzech głównych modułów funkcjonalnych:

- analizator leksykalny - czytając znak po znaku jego zadaniem jest rozpoznawanie leksemów
- analizator składniowy - nadaje znaczenie gramatyczne tokenom otrzymywanym od analizatora leksykalnego
- analizator semantyczny - na podstawie drzewa rozbioru składniowego analizuje semantyczną poprawność instrukcji

## Sposób testowania
Do zweryfikowania poprawności działania interpretera zastosowane zostaną dwa rodzaje testów:

- **testy manualne**
- **testy jednostkowe** sprawdzające poprawność działania głównych komponentów projektu (lekser, parser itp.) w kontekście zarówno poprawnego przetwarzania danych jak i poprawnej obsługi błędów oraz testy sprawdzające działanie całego interpretera.