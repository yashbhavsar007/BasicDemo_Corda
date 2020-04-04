package com.template.contracts

import com.template.states.AccountState
import net.corda.core.contracts.*
import net.corda.core.transactions.LedgerTransaction
import java.math.BigDecimal
// ************
// * Contract *
// ************
class Account_Contract : Contract {
    companion object {
        @JvmStatic
        // Used to identify our contract when building a transaction.
        val ID = "com.template.contracts.Account_Contract"
    }

    interface Commands : CommandData {
        class Check_Account : TypeOnlyCommandData(), Commands
        class ChechConsume : TypeOnlyCommandData(), Commands
    }

    // A transaction is valid if the verify() function of the contract of all the transaction's input and output states
    // does not throw an exception.
    override fun verify(tx: LedgerTransaction) {
        // Verification logic goes here.
        val command = tx.commands.requireSingleCommand<Commands>()

        when (command.value){
            is Commands.Check_Account ->{
                requireThat {
                    val op = tx.outputStates.get(0) as AccountState
                    " Inputs must be emptied " using (tx.inputs.isEmpty())
                    //    " There must be output state " using  (tx.outputStates.isEmpty())
                    //" Name required " using (!op.name.isEmpty())
                }
            }
            is Commands.Check_Account ->{
                requireThat {
                    val op = tx.outputStates.get(0) as AccountState
                    " Inputs must not be emptied " using (!tx.inputs.isEmpty())
                    //    " There must be output state " using  (tx.outputStates.isEmpty())
                    //" Name required " using (!op.name.isEmpty())
                }
            }
        }
    }


}