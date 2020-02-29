package nerd.cave.service.member

import nerd.cave.model.member.*
import nerd.cave.model.product.Product
import nerd.cave.store.mongo.MongoStoreService
import java.time.Clock
import java.time.ZonedDateTime

class MemberServiceImpl(private val clock: Clock, storeService: MongoStoreService): MemberService {
    private val ticketStoreService by lazy { storeService.ticketStoreService }
    private val memberEventStoreService by lazy { storeService.memberEventStoreService }

    override suspend fun canPurchaseOnWechat(memberId: String, products: List<Product>): Boolean {
        val memberDetail = getMemberDetail(memberId)
        return memberDetail.memberType == MemberType.NORMAL &&
            products.all { it.enabled } &&
            products.all { it.payViaWechat }
    }

    override suspend fun getMemberDetail(memberId: String): MemberDetail {
        val memberEvents = memberEventStoreService.queryEvents(memberId).sortedBy { it.time }
        val memberDetail = memberEvents.fold(NormalMember(0) as MemberDetail ) { acc, c ->
            computeNextEvent(acc, c)
        }
        return when(memberDetail) {
            is NormalMember -> NormalMember(ticketStoreService.countNotUsedTickets(memberId))
            else -> memberDetail
        }
    }

    private fun computeNextEvent(currentMemberDetail: MemberDetail, memberEvent: MemberEvent): MemberDetail {
        return when(memberEvent.eventDetail) {
            is YearlyMemberEvent -> extendYearlyMember(currentMemberDetail, memberEvent.time, memberEvent.eventDetail.year)
            is MonthlyMemberEvent -> extendMonthlyMember(currentMemberDetail, memberEvent.time, memberEvent.eventDetail.month)
            else -> currentMemberDetail
        }
    }

    private fun extendYearlyMember(currentMemberDetail: MemberDetail, eventTime: ZonedDateTime, year: Int): MemberDetail {
        val eventDate = eventTime.withZoneSameInstant(clock.zone).toLocalDate()
        return when(currentMemberDetail) {
            is ContractMemberDetail -> {
                if (currentMemberDetail.expiryDate.isBefore(eventDate)) YearlyMember(eventDate.plusYears(year.toLong()).minusDays(1)) else YearlyMember(currentMemberDetail.expiryDate.plusYears(year.toLong()).minusDays(1))
            }
            else -> YearlyMember(eventDate.plusYears(year.toLong()).minusDays(1))
        }
    }

    private fun extendMonthlyMember(currentMemberDetail: MemberDetail, eventTime: ZonedDateTime, month: Int): MemberDetail {
        val eventDate = eventTime.withZoneSameInstant(clock.zone).toLocalDate()
        return when(currentMemberDetail) {
            is ContractMemberDetail -> {
                if (currentMemberDetail.expiryDate.isBefore(eventDate)) {
                    when(currentMemberDetail) {
                        is YearlyMember -> YearlyMember(eventDate.plusMonths(month.toLong()).minusDays(1))
                        else -> MonthlyMember(eventDate.plusMonths(month.toLong()).minusDays(1))
                    }
                } else MonthlyMember(currentMemberDetail.expiryDate.plusMonths(month.toLong()).minusDays(1))
            }
            else -> MonthlyMember(eventDate.plusMonths(month.toLong()).minusDays(1))
        }
    }




}