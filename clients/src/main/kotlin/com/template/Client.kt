package com.template.clients

import com.template.clients
import com.template.states.CashState
import net.corda.client.rpc.CordaRPCClient
import net.corda.core.contracts.StateRef
import net.corda.core.node.services.Vault.StateStatus
import net.corda.core.node.services.vault.QueryCriteria
import net.corda.core.utilities.NetworkHostAndPort
import java.lang.IllegalArgumentException

/**
 * Connects to a Corda node via RPC and performs RPC operations on the node.
 *
 * The RPC connection is configured using command line arguments.
 */
class Client {

    fun main(args: Array<String>) {
        // Create an RPC connection to the node.
        if (args.size != 4) throw IllegalArgumentException("Usage: Client <node address> <rpc username> <rpc password> <CashState value>")
        val nodeAddress = NetworkHostAndPort.parse(args[0])
        val rpcUsername = args[1]
        val rpcPassword = args[2]
        val value = args[3].toInt()
        val client = CordaRPCClient(nodeAddress)
        val clientConnection = client.start(rpcUsername, rpcPassword)
        val proxy = clientConnection.proxy;
        val transactionId = proxy.startFlowDynamic(CreateCashFlow::class.java, value).returnValue.get()
        println("returned transaction Id: $transactionId")
        println("-------------------------------------------")
        val criteria: QueryCriteria = QueryCriteria.VaultQueryCriteria(StateStatus.ALL,setOf(CashState), listOf(StateRef(transactionId, 0)))

        val states = proxy.vaultQueryByCriteria(criteria, CashState::class.java).states
        require(states.isNotEmpty()) {
            "There is no specific CashState" +
                    "-------------------------------------------"
        }
        val (state, ref) = states.single()
        println("Txhash: " + ref.txhash.toString())
        println("Value: " + state.data.value)
        println("Owner: " + state.data.owner.toString())
        println("-------------------------------------------")
        clientConnection.close()
        return
    }
}