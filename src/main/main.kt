import configparser.ConfigParser
import interpreter.Interpreter
import lexer.Lexer
import lexer.source.FileSource
import parser.Parser

fun main(args: Array<String>) {
    try {
        if (args.size != 2)
            throw Exception("Wrong number of program arguments. Expected: 2 (config file path and source file path); Passed: ${args.size}.")
        val configFilePath = args[0]
        val sourceFilePath = args[1]
        val configSource = FileSource(configFilePath)
        val sourceFileSource = FileSource(sourceFilePath)

        val configParser = ConfigParser(Lexer(configSource))
        val config = configParser.parseConfig()

        val sourceParser = Parser(Lexer(sourceFileSource, config.currencyIds))
        val program = sourceParser.parse()

        val interpreter = Interpreter(program, config.exchangeRatesMap)
        interpreter.interpret()
    } catch (e: Exception) {
        print('\n')
        print("${e::class.simpleName}, ${e.message}")
        print('\n')
    }
}
