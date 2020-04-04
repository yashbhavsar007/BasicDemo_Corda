package com.template.flows

import co.paralleluniverse.fibers.Suspendable
import com.template.contracts.Account_Contract
import com.template.states.AccountState
import net.corda.core.contracts.Command
import net.corda.core.contracts.requireThat
import net.corda.core.flows.*
import net.corda.core.identity.Party
import net.corda.core.transactions.SignedTransaction
import net.corda.core.transactions.TransactionBuilder
import org.json.JSONObject

// *********
// * Flows *
// *********

@InitiatingFlow
@StartableByRPC
class IssueFlow(private val Value: Int, private val Receiver : Party) : FlowLogic<String>(){

    @Suspendable
    @Throws(FlowException::class)
    override fun call() : String {
        try{
            val notary = serviceHub.networkMapCache.notaryIdentities[0]
            var IssueState = AccountState(Value,Receiver)
            val IssueCmd = Command(Account_Contract.Commands.Check_Account(), listOf(Receiver.owningKey))

            val txBuilder = TransactionBuilder(notary = notary)
                    .addOutputState(IssueState)
                    .addCommand(IssueCmd)

            // calling the verify method to verify the transaction
            txBuilder.verify(serviceHub)

            // Signing the initial transaction with servicehub
            val signedTx = serviceHub.signInitialTransaction(txBuilder)

            val flowsession = initiateFlow(Receiver)
            val allSignedTransaction = subFlow(CollectSignaturesFlow(signedTx , listOf(flowsession),CollectSignaturesFlow.tracker()))
            subFlow(FinalityFlow(allSignedTransaction, listOf(flowsession)))

            val op = JSONObject()
            op.put("Error","False")
            op.put("Data",allSignedTransaction)
            op.put("Message","Issued Successfully")
            return op.toString()

        }
        catch (e:Exception){
            val op = JSONObject()
            op.put("Error","True")
            op.put("Message",e.message)
            return op.toString()
        }
    }
}


@InitiatedBy(IssueFlow::class)
class IssueFlowResponder( private val flowsession : FlowSession) : FlowLogic<Unit>() {

    @Suspendable
    override fun call() {
        val signedTransactionFlow = object : SignTransactionFlow(flowsession){
            override fun checkTransaction(stx: SignedTransaction) =
                    requireThat {
                        val output = stx.tx.outputs.single().data
                        "This must be an AccountState output contained in transaction " using (output is AccountState)
                    }

        }
        val expectedTxId = subFlow(signedTransactionFlow).id
        subFlow(ReceiveFinalityFlow(flowsession , expectedTxId))

    }
}



@InitiatingFlow
@StartableByRPC
class ConsumeFlow(private val Value: Int) : FlowLogic<String>(){

    @Suspendable
    @Throws(FlowException::class)
    override fun call() : String {
        try{
            val notary = serviceHub.networkMapCache.notaryIdentities[0]

            val Consume_State_Vault = serviceHub.vaultService.queryBy(AccountState::class.java).states
            val State_Vault = Consume_State_Vault.get(Consume_State_Vault.size-1).state.data
            val InState = serviceHub.toStateAndRef<AccountState>(Consume_State_Vault.get(Consume_State_Vault.size-1).ref)
            val OpState = AccountState(State_Vault.value - Value,ourIdentity)
            val IssueCmd = Command(Account_Contract.Commands.ChechConsume(), listOf(ourIdentity.owningKey))

            val txBuilder = TransactionBuilder(notary)
                    .addInputState(InState)
                    .addOutputState(OpState)
                    .addCommand(IssueCmd)
            val signedTx = serviceHub.signInitialTransaction(txBuilder)

            // Broadcasting the transaction
            subFlow(FinalityFlow(signedTx, emptyList()))
            val op = JSONObject()
            op.put("Error","False")
            op.put("Message","Consumed Successfully")
            return op.toString()
        }
        catch (e:Exception){
            val op = JSONObject()
            op.put("Error","True")
            op.put("Message",e.message)
            return op.toString()
        }
    }
}
