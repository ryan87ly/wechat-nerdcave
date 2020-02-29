package nerd.cave.store

import nerd.cave.component.LifeCycle
import nerd.cave.model.ticket.Ticket

interface TicketStoreService: LifeCycle {
    suspend fun insertTicket(ticket: Ticket)
    suspend fun latestNotUsedTicket(memberId: String): Ticket?
    suspend fun markUsed(ticketId: String, tokenId: String): Boolean
    suspend fun countNotUsedTickets(memberId: String): Long
}