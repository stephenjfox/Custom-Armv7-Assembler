package assembler

import model.GlobalConfig
import model.Logger
import model.Logger.d
import model.Logger.v
import model.asString
import model.splitOnSpace
import java.io.File
import java.nio.file.Files
import kotlin.text.Regex

/**
 * Intended to read the file and output tokens.
 *
 * Created by stephen on 11/28/15.
 */
class Lexer(val sourceFile : File) {

    private val verboseTyping = GlobalConfig.getBoolean("verbose")
    private val debugTyping = GlobalConfig.getBoolean("debug") || verboseTyping

    private val REGEX_WRAP_CHAR = '%'
    private val LABEL_DEF_REGEX : Regex = "$REGEX_WRAP_CHAR\\w+$REGEX_WRAP_CHAR".toRegex()
    private val COMMENT_CHAR = '#'
    private val labelMap = LabelUsageMap()

    fun lex() : TokenStream {
        val choiceDelimiter = "|"
        if (debugTyping) {
            println("REGEX_WRAP_CHAR = ${REGEX_WRAP_CHAR}")
            println("LABEL_DEF_REGEX = ${LABEL_DEF_REGEX.pattern}")
        }
        /*
        * 1) Read a file
        *   a. get filepath string
        *   b. parse every line, building a string array
        *   c. THIS WILL NEED AN EDIT, when supporting sub-routines
        */
        val concat = getSourceLinesConcat(choiceDelimiter)
        /*
        * 2) For each (supposed) statement:
        *   a. Read the first string to determine the function to call
        *   b. the remaining args from be the parameters for the function
        */
        val lines = concat.split(choiceDelimiter)

        // scratch: this first pass is to build a "symbol table"
        for ((index, sourceLine) in lines.withIndex()) {
            val lineNumber = index + 1

            val spaceSeparatedInputs = sourceLine.splitOnSpace(',')


            if (LABEL_DEF_REGEX.containsMatchIn(sourceLine)) {
                Logger.d("Line ($sourceLine) contains regex match")
                val labelName = sourceLine.subSequence(
                        sourceLine.indexOf(REGEX_WRAP_CHAR) + 1,
                        sourceLine.lastIndexOf(REGEX_WRAP_CHAR)).asString()

                if (spaceSeparatedInputs[0].matches(LABEL_DEF_REGEX)) {
                    labelMap.putDefinitionSite(labelName, lineNumber)
                }
                else labelMap.putUsageSite(labelName, lineNumber)
            }
        }

        Logger.v(labelMap)

        // scratch: pass #2 is to fill the needs
        for ((currentIndex, line) in lines.withIndex()) {
            val spaceSeparatedInputs = line.splitOnSpace(',')
            d("split = $spaceSeparatedInputs")
            v("split.size = ${spaceSeparatedInputs.size}")

            tokenizeLine(spaceSeparatedInputs, currentIndex + 1)
            if ( currentIndex < lines.lastIndex) {
                TokenStream.yield(Tokens.create(TokenType.NewLine, "I DON'T NEED TEXT HERE"))
            }
        }
        /*
        * 3) Each function invocation will perform the following
        *   a. Determine proper build nibble array
        *   b. Invoke Ryli's program?
        */
        return TokenStream.getTokenStream()
    }

    private fun tokenizeLine(listOfStrings : List<String>, failureLine : Int) {
        v("Going to produce tokens for: $listOfStrings")

        val firstString = listOfStrings[0]
        when (listOfStrings.size) {
            2 -> {
                // there's a limit to the options
                /*
                 * there are other commands, but I don't think I'll need 'em,
                 * save the BLK (branch with link) and BXN (?) that I can handle like the MoveCommand
                 * branch: B<c> <imm24>
                 */
                val branchToken = Tokens.create(TokenType.Branch, firstString)
                val imm24Token = Tokens.create(TokenType.Immediate, listOfStrings[1])
                TokenStream.yieldSequential(branchToken, imm24Token)
            }
            3 -> {
                // there are... more options
                // check if the first word starts with "MOV"
                if (firstString.startsWith("MOV")) {
                    /*
                     * move: MOVW<c> <Rn> <imm16|Rm> output will be different depending
                     *       MOVT<c> <Rn> <imm16|Rm>
                     *       MOV(S)<c> <Rd>
                     */
                    moveTokenYield(listOfStrings)
                } else if (firstString.startsWith("LDR")) {
                    /*
                     * load: LDR<c> <Rt> <Rn>
                     */
                    loadStoreRegisterTokenYield(listOfStrings)
                } else if (firstString.startsWith("STR")) {
                    /*
                     * store: STR<c> <Rt> <Rn>
                     */
                    loadStoreRegisterTokenYield(listOfStrings)
                } else {
                    // TODO: let ADD/SUB use 3, 4, 5 arguments
                    println("Syntax error on line $failureLine. Arithmetic operation must have at least three arguments")
                    error("code: $listOfStrings")
                }
            }
            4, 5 -> {
                // there should be less options here.
                /* There are 30 Load commands, 25 of which are for ARM. I don't want
                 * to write those 15 cases.
                 * scratch: for now, read the grammar. Other types will be dealt with later
                 * load: LDR<c> <Rt> <Rn> <imm12>
                 *              <Rn> (<Rt>) <imm12> // I  don't like this
                 * store: STR<c> <Rt> <Rn> <imm12>
                 *               <Rn> (<Rt>) <imm12> // see the above
                 */
                if (firstString.startsWith("STR") || firstString.startsWith("LDR")) {
                    loadStoreRegisterTokenYield(listOfStrings)
                }
                /*
                 * add: ADD<c> <Rd> <Rn> <imm12>
                 *
                 * subtract: SUB<c> <Rd> <Rn> <imm12>
                 */
                else if (firstString.startsWith("ADD") || firstString.startsWith("SUB")) {
                    addSubtractTokenYield(listOfStrings)
                }
                /*
                 * or: ORR(S)<c> <Rd> <Rn> <imm12>
                 *
                 */
                else if (firstString.startsWith("OR")) {
                    orFunctionTokenYield(listOfStrings)
                } else {
                    Logger.e("Failed to tokenize arguments: $listOfStrings")
                    throw IllegalArgumentException("Probable syntax error on line: $failureLine")
                }
            }
            6 -> {
                /*
                 * add: ADD<c> <Rd> <Rn> <imm12>
                 *
                 * subtract: SUB{S}<c> <Rd>, <Rn>, <Rm>, <type> <Rs>
                 */
                if (firstString.startsWith("ADD") || firstString.startsWith("SUB")) {
                    addSubtractTokenYield(listOfStrings)
                }
                /*
                 * or: ORR{S}<c> <Rd>, <Rn>, <Rm>, <type> <Rs>
                 *
                 */
                else if (firstString.startsWith("ORR")) {
                    orFunctionTokenYield(listOfStrings)
                } else {
                    error("Probable syntax error on line: $failureLine\nThrown from Lexer.kt:150ish")
                }
            }
            else -> {
                d("listOfStrings = [$listOfStrings], failureLine = [$failureLine]")
                throw IllegalArgumentException("Bad arguments were passed at line #$failureLine")
            }
        }
    }

    private fun moveTokenYield(listOfStrings : List<String>) {
        val moveToken = Tokens.create(TokenType.Move, listOfStrings[0])
        val registerToken = Tokens.create(TokenType.Register, listOfStrings[1])
        // if starts with 'R',
        val secondParam = listOfStrings[2]
        val immediateOrRegisterToken = (Tokens.create(registerOrImmediate(secondParam), secondParam))

        TokenStream.yieldSequential(moveToken, registerToken, immediateOrRegisterToken)
    }

    private fun loadStoreRegisterTokenYield(stringLineInputs : List<String>) {

        val principalToken = Tokens.create(getStrOrLdr(stringLineInputs[0]), stringLineInputs[0])
        val register1Token = Tokens.create(TokenType.Register, stringLineInputs[1])
        val register2Token = Tokens.create(TokenType.Register, stringLineInputs[2])

        v("stringLineInputs = $stringLineInputs yielded tokens:" +
                "\tload/strToken = {$principalToken}" +
                "\tR1Token = {$register1Token}" +
                "\tR2Token = {$register2Token}")

        TokenStream.yieldSequential(principalToken, register1Token, register2Token)

        if (stringLineInputs.size > 3) {

            val fourthToken = Tokens.create(registerOrImmediate(stringLineInputs[3]),
                    stringLineInputs[3])

            if (stringLineInputs.size == 4) {
                TokenStream.yield(fourthToken)
            } else if (stringLineInputs.size == 5) {
                // FIXME: can be a register or shift. Please fix this soon, because...
                // commands like STREXD (write a 64-bit value to TWO registers) are going to be a thing
                val shiftToken = Tokens.create(TokenType.Shift, stringLineInputs[4])
                TokenStream.yieldSequential(fourthToken, shiftToken)
            } else {
                error("there was an 'LDR' instruction that was too long.\nInstruction = ${stringLineInputs}")
            }
        }
    }

    private fun addSubtractTokenYield(operands : List<String>) {
        val principalToken = Tokens.create(addOrSubTokenType(operands[0]), operands[0])
        val register1Token = Tokens.create(TokenType.Register, operands[1])
        val register2Token = Tokens.create(TokenType.Register, operands[2])

        val semanticOperand2Token = Tokens.create(registerOrImmediate(operands[3]), operands[3])

        TokenStream.yieldSequential(principalToken, register1Token, register2Token, semanticOperand2Token)
        if (operands.size > 4) {

            if (operands.size == 5) {
                // it's a shift
                val shiftToken = Tokens.create(TokenType.Shift, operands[4])
                TokenStream.yield(shiftToken)
            } else if (operands.size == 6) {
                // token5 is a "type", which is just a 2-bit value
                // token6 is a register
                // for more see "Arm Architecture Reference Manual ARMv7-A (or '-R')":
                // A8.8.224 SUB (register-shifted register)
                Logger.w("6 arguments passed to abbSubtractTokenYield")
            }
            else {
                // we should NEVER be able to get here, because I don't allow codes that long
                // but to be safe
                Logger.e("You did the impossible and passed 7 arguments into this func")
                throw IllegalStateException("NOT ALLOWED: 7 operands = $operands")
            }

        }
    }

    private fun orFunctionTokenYield(orChunks : List<String>) {
        val token = Tokens.create(TokenType.Or, orChunks[0])
        val r1 = Tokens.create(TokenType.Register, orChunks[1])
        val r2 = Tokens.create(TokenType.Register, orChunks[2])
        var regOrImmediate = Tokens.create(registerOrImmediate(orChunks[3]), orChunks[3])

        TokenStream.yieldSequential(token, r1, r2, regOrImmediate)
        if (orChunks.size > 4) {
            if (orChunks.size == 5) {
                // we've got an immediate shift value
                val immShift = Tokens.create(TokenType.Shift, orChunks[4])

                TokenStream.yield(immShift)
            } else if (orChunks.size == 6) {
                // it's a 2-bit type and a 4-bit register
                val typeTok = Tokens.create(TokenType.Type2Bit, orChunks[4])
                val finRegister = Tokens.create(TokenType.Register, orChunks[5])

                TokenStream.yieldSequential(typeTok, finRegister)
            }
        }
    }


    /*  HELPER FUNCTIONS  */

    private fun addOrSubTokenType(addOrSub : String) : TokenType {
        return (if (addOrSub[0] == 'S' && addOrSub[1] == 'U' && addOrSub[2] == 'B')
            TokenType.Subtract
        else TokenType.Add)
    }

    private fun getStrOrLdr(string : String) : TokenType {
        return if (string.startsWith("LDR")) {
            TokenType.Load
        } else TokenType.Store
    }

    private fun registerOrImmediate(ambiguousAssembly : String) =
            (if (ambiguousAssembly[0] == 'R') TokenType.Register else TokenType.Immediate)

    private fun getSourceLinesConcat(delimiter : String = "|") : String {
        v("Reading files")
        val allLines : List<String> = Files.readAllLines(sourceFile.toPath())
        if (debugTyping) {
            v("The following are the lines of the text file...")
            allLines.forEach { d(it) }
        }

        val reduceRight : String = allLines.map {
            val poundEx = it.indexOf(char = COMMENT_CHAR)
            val indOfComment = if (poundEx == -1) {
                it.length // scratch: because endIndex is "up until" or exclusively bound
            } else if (poundEx == 0) {
                0
            } else {
                poundEx - 1
            }
            it.substring(startIndex = 0, endIndex = indOfComment)
        }.filter { it.length > 0 }.reduce { str, s -> str.plus(delimiter).plus(s) }

        v("Concat: $reduceRight")

        return reduceRight
    }

}
