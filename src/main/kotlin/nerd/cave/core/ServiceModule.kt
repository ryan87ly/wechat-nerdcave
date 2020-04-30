package nerd.cave.core

import com.google.inject.AbstractModule
import nerd.cave.service.branch.BranchService
import nerd.cave.service.branch.BranchServiceImpl
import nerd.cave.service.checkin.CheckInNumberGenerator
import nerd.cave.service.checkin.CheckInNumberGeneratorMongoImpl
import nerd.cave.service.checkin.CheckInService
import nerd.cave.service.checkin.CheckInServiceImpl
import nerd.cave.service.holiday.HolidayService
import nerd.cave.service.holiday.HolidayServiceImpl
import nerd.cave.service.member.MemberService
import nerd.cave.service.member.MemberServiceImpl
import nerd.cave.service.order.OrderService
import nerd.cave.service.order.OrderServiceImpl
import javax.inject.Singleton

class ServiceModule: AbstractModule() {

    override fun configure() {
        bind(HolidayService::class.java).to(HolidayServiceImpl::class.java).`in`(Singleton::class.java)
        bind(MemberService::class.java).to(MemberServiceImpl::class.java).`in`(Singleton::class.java)
        bind(OrderService::class.java).to(OrderServiceImpl::class.java).`in`(Singleton::class.java)
        bind(CheckInNumberGenerator::class.java).to(CheckInNumberGeneratorMongoImpl::class.java).`in`(Singleton::class.java)
        bind(BranchService::class.java).to(BranchServiceImpl::class.java).`in`(Singleton::class.java)
        bind(CheckInService::class.java).to(CheckInServiceImpl::class.java).`in`(Singleton::class.java)
    }
}