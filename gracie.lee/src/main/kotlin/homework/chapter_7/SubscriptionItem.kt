package homework.chapter_7

import jakarta.persistence.*

@Entity(name = "Ch7SubscriptionItemKt")
@Table(name = "subscription_item_ch7_kt")
@DiscriminatorValue("SUBSCRIPTION") // DTYPE에 들어갈 값
class SubscriptionItem(
    @Column(nullable = false)
    var billingCycleDays: Int = 0,

    @Column(nullable = false)
    var trialDays: Int = 0
) : Item()
