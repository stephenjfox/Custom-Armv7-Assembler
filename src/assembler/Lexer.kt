package assembler

import assembler.TokenStream.Companion.yield
import assembler.TokenStream.Companion.yieldSequential
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
    private val LABEL_REGEX: Regex = "$REGEX_WRAP_CHAR\\w+$REGEX_WRAP_CHAR".toRegex()
    private val COMMENT_CHAR = '#'
    private val labelMap = LabelUsageMap()

    /* todo: check notebook for reflection on stack
    aly showed that by using a "call" keyword and label name, she could get
    subroutine behavior. Stack is just some register address and filling the
    memory at that point. (I think Halladay just had problems with tracking
    the things and the larger abstraction, because I don't see these major
    that he keeps mentions */

    // Subroutines video says:
    /* MOV R0, R15 # to account for the instruction counter
    *  ADD R0, R0, 4 # 4 == (-2 + 3) * 4, because the pipeline
    *  BAL Subroutine
    *  // R0 SHOULD point here, but I - Stephen James Fox - don't believe
    *
    *  Subroutine:
    *  // Some assembly body
    *  MOV R15, R0
    *
    *  30s later in the video, Halladay explains that ARM does it for us. AGAIN
    *
    *  BL Subroutine # store succeeding address in R14
    *
    *  Subroutine:
    *  // Some assembly body
    *  MOV R15, R14
    *
    *  THE STACK IS NECESSITATED BY THE NEED TO KNOW THAT YOUR REGISTERS WILL
    *  BE SAFE
    */

    fun lex() : TokenStream {
        val choiceDelimiter = "|"
        if (debugTyping) {
            println("REGEX_WRAP_CHAR = $REGEX_WRAP_CHAR")
            println("LABEL_DEF_REGEX = ${LABEL_REGEX.pattern}")
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


            if (LABEL_REGEX.containsMatchIn(sourceLine)) {
                Logger.d("Line ($sourceLine) contains regex match")
                val labelName = stripSpecialRegex(sourceLine)

                if (spaceSeparatedInputs[0].matches(LABEL_REGEX)) {
                    labelMap.putDefinitionSite(labelName, lineNumber)
                }
                else {
                    labelMap.putUsageSite(labelName, lineNumber)
                }
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
                yield(Tokens.create(TokenType.NewLine, "I DON'T NEED TEXT HERE"))
            }
            /* TODO: read the following block comment
            * I need a stack (push = increment before, pop = increment after)
            * IN MY ASSEMBLY TEXT FILES: R13 is the stack head
            * I need to lex a PUSH and POP instruction (that will take the most time)
            * They will both need to be checked at every level.
            * Their only arguments are Registers, so that is easy (for-loop linestrings variable)
            * I need to "initialize" R13, somewhere. Idc, MOVT R13 0x80.
            *   - No one seems to know if something more is needed.
            */
        }
        /*
        * 3) Each function invocation will perform the following
        *   a. Determine proper build nibble array
        *   b. Invoke Ryli's program?
        */
        return TokenStream.getTokenStream()
    }

    private fun tokenizeLine(listOfStrings: List<String>, currentInstructionLine: Int) {
        v("Going to produce tokens for: $listOfStrings")
        val lineStrings: List<String>
        val firstString: String

        // this is a bit hackish, but it fills the need
        if (listOfStrings[0].matches(LABEL_REGEX)) {
            lineStrings = listOfStrings.subList(1, listOfStrings.size)
        } else {
            lineStrings = listOfStrings
        }

        firstString = lineStrings[0]

        when (lineStrings.size) {
            2 -> {
                // there's a limit to the options
                /*
                 * there are other commands, but I don't think I'll need 'em,
                 * save the BLK (branch with link) and BXN (?) that I can handle like the MoveCommand
                 * branch: B<c> <imm24>
                 */
                if (firstString.startsWith("B")) {
                    branchTokenYield(currentInstructionLine, firstString, lineStrings)
                }
                /*
                 * push: PUSH<c> <R_1> to <R_15>
                 * pop: POP<c> <R_1> to <R_15>
                 */
                else if (firstString.startsWith("P")) {
                    stackManyTokenYield(firstString, lineStrings.subList(1, lineStrings.size))
                } else {
                    Logger.e("Syntax $firstString a valid token")
                    throw IllegalArgumentException("Dirty and short inputs $lineStrings")
                }
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
                    moveTokenYield(lineStrings)
                } else if (firstString.startsWith("LDR")) {
                    /*
                     * load: LDR<c> <Rt> <Rn>
                     */
                    loadStoreRegisterTokenYield(lineStrings)
                } else if (firstString.startsWith("STR")) {
                    /*
                     * store: STR<c> <Rt> <Rn>
                     */
                    loadStoreRegisterTokenYield(lineStrings)
                } else if (firstString.startsWith("P")) {
                    /*
                     * push: PUSH<c> <R_1> to <R_15>
                     * pop: POP<c> <R_1> to <R_15>
                     */
                    stackManyTokenYield(firstString, lineStrings.subList(1, lineStrings.size))
                } else {
                    println("Syntax error on line $currentInstructionLine. Arithmetic operation must have at least three arguments")
                    error("code: $lineStrings")
                }
            }
            4, 5 -> {
                // there should be less options here.
                /* There are 30 Load commands, 25 of which are for ARM. I don't want
                 * to write those 15 cases.
                 * scratch: for now, read the grammar. Other types will be dealt with later
                 * load: LDR<c> <Rt> <Rn> <imm12>
                 * store: STR<c> <Rt> <Rn> <imm12>
                 */
                if (firstString.startsWith("STR") || firstString.startsWith("LDR")) {
                    loadStoreRegisterTokenYield(lineStrings)
                }
                /*
                 * add: ADD<c> <Rd> <Rn> <imm12>
                 * subtract: SUB<c> <Rd> <Rn> <imm12>
                 */
                else if (firstString.startsWith("ADD") || firstString.startsWith("SUB")) {
                    addSubtractTokenYield(lineStrings)
                }
                /*
                 * push: PUSH<c> <R_1> to <R_15>
                 * pop: POP<c> <R_1> to <R_15>
                 */
                else if (firstString.startsWith("P")) {
                    stackManyTokenYield(firstString, lineStrings.subList(1, lineStrings.size))
                }
                /*
                 * or: ORR(S)<c> <Rd> <Rn> <imm12>
                 */
                else if (firstString.startsWith("OR")) {
                    orFunctionTokenYield(lineStrings)
                } else {
                    Logger.e("Failed to tokenize arguments: $lineStrings")
                    throw IllegalArgumentException("Probable syntax error on line: $currentInstructionLine")
                }
            }
            6 -> {
                /*
                 * add: ADD<c> <Rd> <Rn> <imm12>
                 * subtract: SUB{S}<c> <Rd>, <Rn>, <Rm>, <type> <Rs>
                 */
                if (firstString.startsWith("ADD") || firstString.startsWith("SUB")) {
                    addSubtractTokenYield(lineStrings)
                }
                /*
                 * push: PUSH<c> <R_1> to <R_15>
                 * pop: POP<c> <R_1> to <R_15>
                 */
                else if (firstString.startsWith("P")) {
                    stackManyTokenYield(firstString, lineStrings.subList(1, lineStrings.size))
                }
                /*
                 * or: ORR{S}<c> <Rd>, <Rn>, <Rm>, <type> <Rs>
                 */
                else if (firstString.startsWith("ORR")) {
                    orFunctionTokenYield(lineStrings)
                } else {
                    error("Probable syntax error on line: $currentInstructionLine")
                }
            }
            in 7..15 -> {
                /*
                 * push: PUSH<c> <R_1> to <R_15>
                 * pop: POP<c> <R_1> to <R_15>
                 */
                if (firstString.startsWith("P")) {
                    stackManyTokenYield(firstString, lineStrings.subList(1, lineStrings.size))
                }
                else {
                    throw IllegalArgumentException("Probable syntax error on line: $currentInstructionLine\nOnly PUSH/POP should be this long of a command")
                }
            }
            else -> {
                d("listOfStrings = [$lineStrings], failureLine = [$currentInstructionLine]")
                throw IllegalArgumentException("Bad arguments were passed at line #$currentInstructionLine")
            }
        }
    }

    /**
     * Because we're using R13 as the true stack pointer, we'll be using
     * A8.8.132 (POP) encoding A1 and A8.8.133 (PUSH) encoding A1
     * Will yield to the [TokenStream]:
     * a) either [PushManyToken] or [PopManyToken] and
     * b) at least one [RegisterToken]
     */
    private fun stackManyTokenYield(stackOpString: String, subListOfRegisters: List<String>) {
        val opType = checkPushOrPop(stackOpString)
        val stackOpToken = Tokens.create(opType, stackOpString)
        yield(stackOpToken)

        for ((index, registerString) in subListOfRegisters.withIndex()) {
            // try make register
            try {
                val newToken = Tokens.create(TokenType.Register, registerString)
                yield(newToken)
            } catch (e: Exception) {
                throw IllegalArgumentException("SYNTAX ERROR - Register #$index wasn't parse-able: $registerString")
            }
        }
    }

    private fun moveTokenYield(listOfStrings : List<String>) {
        val moveToken = Tokens.create(TokenType.Move, listOfStrings[0])
        val registerToken = Tokens.create(TokenType.Register, listOfStrings[1])
        // if starts with 'R',
        val secondParam = listOfStrings[2]
        val immediateOrRegisterToken = (Tokens.create(registerOrImmediate(secondParam), secondParam))

        yieldSequential(moveToken, registerToken, immediateOrRegisterToken)
    }

    private fun branchTokenYield(currentInstructionLine: Int, firstString: String, lineStrings: List<String>) {
        val branchToken = if (firstString.startsWith("BLK")) {
            Tokens.create(TokenType.BranchWithLink, firstString)
        } else {
            Tokens.create(TokenType.Branch, firstString)
        }
        val imm24Token = (if (lineStrings[1].matches(LABEL_REGEX)) {

            val name = stripSpecialRegex(lineStrings[1])
            val labelUsageRecord = labelMap[name]!!
            v("labelUsageRecord = $labelUsageRecord")

            val intRelocation = labelUsageRecord.definitionLine - currentInstructionLine - 2
            val hexString = hexStringWidthCheck(Integer.toHexString(intRelocation), 6, intRelocation > -1)
            d("hexString = $hexString")

            Tokens.create(TokenType.Immediate, "0x$hexString")

        } else {
            Tokens.create(TokenType.Immediate, lineStrings[1])
        })
        yieldSequential(branchToken, imm24Token)
    }

    private fun loadStoreRegisterTokenYield(stringLineInputs : List<String>) {

        val principalToken = Tokens.create(getStrOrLdr(stringLineInputs[0]), stringLineInputs[0])
        val register1Token = Tokens.create(TokenType.Register, stringLineInputs[1])
        val register2Token = Tokens.create(TokenType.Register, stringLineInputs[2])

        v("stringLineInputs = $stringLineInputs yielded tokens:" +
                "\tload/strToken = {$principalToken}" +
                "\tR1Token = {$register1Token}" +
                "\tR2Token = {$register2Token}")

        yieldSequential(principalToken, register1Token, register2Token)

        if (stringLineInputs.size > 3) {

            val fourthToken = Tokens.create(registerOrImmediate(stringLineInputs[3]),
                    stringLineInputs[3])

            if (stringLineInputs.size == 4) {
                yield(fourthToken)
            } else if (stringLineInputs.size == 5) {
                // FIXME: can be a register or shift. Please fix this soon, because...
                // commands like STREXD (write a 64-bit value to TWO registers) are going to be a thing
                val shiftToken = Tokens.create(TokenType.Shift, stringLineInputs[4])
                yieldSequential(fourthToken, shiftToken)
            } else {
                error("there was an 'LDR' instruction that was too long.\nInstruction = $stringLineInputs")
            }
        }
    }

    private fun addSubtractTokenYield(operands : List<String>) {
        val principalToken = Tokens.create(addOrSubTokenType(operands[0]), operands[0])
        val register1Token = Tokens.create(TokenType.Register, operands[1])
        val register2Token = Tokens.create(TokenType.Register, operands[2])

        val semanticOperand2Token = Tokens.create(registerOrImmediate(operands[3]), operands[3])

        yieldSequential(principalToken, register1Token, register2Token, semanticOperand2Token)
        if (operands.size > 4) {

            if (operands.size == 5) {
                // it's a shift
                val shiftToken = Tokens.create(TokenType.Shift, operands[4])
                yield(shiftToken)
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

        yieldSequential(token, r1, r2, regOrImmediate)
        if (orChunks.size > 4) {
            if (orChunks.size == 5) {
                // we've got an immediate shift value
                val immShift = Tokens.create(TokenType.Shift, orChunks[4])

                yield(immShift)
            } else if (orChunks.size == 6) {
                // it's a 2-bit type and a 4-bit register
                val typeTok = Tokens.create(TokenType.Type2Bit, orChunks[4])
                val finRegister = Tokens.create(TokenType.Register, orChunks[5])

                yieldSequential(typeTok, finRegister)
            }
            else {
                throw IllegalStateException("Too many arguments to the ORR statement")
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

    private fun checkPushOrPop(stackOpString: String) =
            if (stackOpString.startsWith("POP")) TokenType.Pop else TokenType.Push

    private fun registerOrImmediate(ambiguousAssembly : String) =
            (if (ambiguousAssembly[0] == 'R') TokenType.Register else TokenType.Immediate)

    private fun stripSpecialRegex(sourceString: String): String {
        return sourceString.subSequence(
                sourceString.indexOf(REGEX_WRAP_CHAR) + 1,
                sourceString.lastIndexOf(REGEX_WRAP_CHAR)).asString()
    }

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
            it.substring(beginIndex = 0, endIndex = indOfComment)
        }.filter { it.length > 0 }.reduce { str, s -> str.plus(delimiter).plus(s) }

        v("Concat: $reduceRight")

        return reduceRight
    }

    private fun hexStringWidthCheck(hexString: String, width: Int, shouldBePositive: Boolean) : String {
        val extendHex = if (shouldBePositive) "0" else "F"

        return if (hexString.length > width) {
            hexString.subSequence(hexString.length - width, hexString.length).asString()
        } else {
            val diff = width - hexString.length
            extendHex.repeat(diff) + hexString
        }
    }
}
