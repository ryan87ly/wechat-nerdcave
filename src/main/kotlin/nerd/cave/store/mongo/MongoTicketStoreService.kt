package nerd.cave.store.mongo

import com.mongodb.client.model.Filters.and
import nerd.cave.model.api.ticket.Ticket
import nerd.cave.model.api.ticket.TicketStatus
import nerd.cave.store.TicketStoreService
import org.litote.kmongo.*

class MongoTicketStoreService(mongoStoreService: MongoStoreService): TicketStoreService {
    private val collection by lazy { mongoStoreService.getCollection<Ticket>() }

    override suspend fun insertTicket(ticket: Ticket) {
        collection.insertOne(ticket)
    }

    override suspend fun latestNotUsedTicket(memberId: String): Ticket? {
        val query = and(
            Ticket::memberId eq memberId,
            Ticket::status eq TicketStatus.NOT_USED
        )
        return collection.find(query).sort(orderBy(Ticket::purchaseTime, ascending = false)).limit(1).first()
    }

    override suspend fun markUsed(ticketId: String, tokenId: String): Boolean {
        val query = and(
            Ticket::id eq ticketId,
            Ticket::status eq TicketStatus.NOT_USED
        )
        val update = combine(
            setValue(Ticket::status, TicketStatus.USED),
            setValue(Ticket::tokenId, tokenId)
        )
        return collection.updateOne(query, update).succeedUpdateOne()
    }

    override suspend fun countNotUsedTickets(memberId: String): Long {
        val query = and(
            "memberId" eq memberId,
            Ticket::status eq TicketStatus.NOT_USED
        )
        return collection.countDocuments(query)
    }
}