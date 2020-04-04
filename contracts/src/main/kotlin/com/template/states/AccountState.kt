package com.template.states

import com.template.contracts.Account_Contract
import net.corda.core.contracts.BelongsToContract
import net.corda.core.contracts.ContractState
import net.corda.core.flows.InitiatingFlow
import net.corda.core.flows.StartableByRPC
import net.corda.core.identity.AbstractParty
import net.corda.core.identity.Party

// *********
// * State *
// *********
@InitiatingFlow
@StartableByRPC
@BelongsToContract(Account_Contract::class)
class AccountState( val value: Int,
                    val owner : Party
                    ) : ContractState {

    override val participants get() = listOf(owner)

}