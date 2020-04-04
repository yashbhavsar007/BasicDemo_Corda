package com.macqueen.webserver

import org.slf4j.LoggerFactory
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
import javax.servlet.http.HttpServletRequest
import net.corda.core.messaging.startTrackedFlow
import com.macqueen.flows.AccountFlow
import net.corda.core.utilities.getOrThrow
import org.springframework.http.HttpStatus
import java.util.LinkedHashMap

/**
 * Define your API endpoints here.
 */
@RestController
@RequestMapping("/") // The paths for HTTP requests are relative to this base path.
class Controller(rpc: NodeRPCConnection) {

    companion object {
        private val logger = LoggerFactory.getLogger(RestController::class.java)
    }

    private val proxy = rpc.proxy

    @GetMapping(value = ["/templateendpoint"], produces = ["text/plain"])
    private fun templateendpoint(): String {
        return "Define an endpoint here."
    }

    @PostMapping(value = "/CreateAccount",produces = ["application/json"],  headers = ["Content-Type=application/x-www-form-urlencoded"])
    private fun CallingFlow(request:HttpServletRequest):ResponseEntity<LinkedHashMap<String, String>>{
       val name = request.getParameter("name")
       // val n = request.queryString.get(0)
        val map = LinkedHashMap<String,String>()
        map.clear()
        if(name.isEmpty()){
            return ResponseEntity.badRequest().body(map)
        }
//
         //return ResponseEntity.status(HttpStatus.CREATED).body("PartyName is ${request.getParameterNames()} Amount is ${request.getParameter("partyName")} and  ${Amount.parseCurrency("10 USD")} .\n")
        return try{
            val signedTx = proxy.startTrackedFlow(::AccountFlow, name.toString()).returnValue.getOrThrow()
            val l = signedTx.get("CipherText")
            ResponseEntity.status(HttpStatus.CREATED).body(signedTx)

        } catch (ex:Throwable) {
            val map = LinkedHashMap<String,String>()
            logger.error(ex.message, ex)
            map.put("Error",ex.toString())
            ResponseEntity.badRequest().body(map)
        }
    }

}