package blackjack.domain.player

import blackjack.domain.card.Card
import blackjack.domain.card.Character
import blackjack.domain.card.Suit
import blackjack.domain.cards.HandCards
import io.kotest.core.spec.style.StringSpec
import io.kotest.matchers.shouldBe
import org.assertj.core.api.Assertions
import java.lang.RuntimeException

class PlayerTest : StringSpec({
    "Player 는 Hit 일 때 hit 할 수 있다" {
        val hand = Hand(HandCards(mutableListOf(Card(Suit.Spade, Character.Jack), Card(Suit.Clover, Character.Eight))))
        val idlePlayer = Player("aaa", hand)

        idlePlayer.state shouldBe PlayerState.Hit
        idlePlayer.hit()
        idlePlayer.state shouldBe PlayerState.Hit
    }

    "Player 가 addCard 하면 Player 의 hand 의 카드 수는 증가해야 한다" {
        val hand = Hand(HandCards(mutableListOf(Card(Suit.Spade, Character.Jack), Card(Suit.Clover, Character.Eight))))
        val player = Player("aaa", hand)

        hand.valueSum() shouldBe 18

        player.hit()
        val prevCardCount = player.hand.handCards.size
        player.addCard(Card(Suit.Heart, Character.Seven))
        player.hand.handCards.size shouldBe prevCardCount + 1
    }

    "Player 가 bust 상태가 되면 hit 할 수 없다" {
        val bustHand = Hand(
            HandCards(
                mutableListOf(
                    Card(Suit.Spade, Character.Three),
                    Card(Suit.Clover, Character.Jack),
                )
            )
        )
        val bustPlayer = Player("bust", bustHand)

        bustPlayer.addCard(Card(Suit.Diamond, Character.Nine))

        bustHand.isBust() shouldBe true
        bustPlayer.state shouldBe PlayerState.Bust

        Assertions.assertThatThrownBy {
            bustPlayer.hit()
        }.isInstanceOf(IllegalStateException::class.java)
            .hasMessageContaining("Invalid state transition")
    }

    "Player 가 blackjack 상태가 되면 hit 할 수 없다" {
        val blackjackHand = Hand(
            HandCards(
                mutableListOf(
                    Card(Suit.Spade, Character.Jack),
                    Card(Suit.Clover, Character.Ace),
                )
            )
        )
        val blackjackPlayer = Player("bj", blackjackHand)

        blackjackPlayer.addCard(Card(Suit.Diamond, Character.Ten))

        blackjackHand.isBlackjack() shouldBe true
        blackjackPlayer.state shouldBe PlayerState.Blackjack

        Assertions.assertThatThrownBy {
            blackjackPlayer.hit()
        }.isInstanceOf(RuntimeException::class.java)
            .hasMessageContaining("Invalid state transition")
    }

    "Player 는 stay 후엔 다시 hit 할 수 없다" {
        val hand = Hand(HandCards(mutableListOf(Card(Suit.Spade, Character.Jack), Card(Suit.Clover, Character.Eight))))
        val stayPlayer = Player("aaa", hand)
        stayPlayer.stay()

        stayPlayer.state shouldBe PlayerState.Stay

        Assertions.assertThatThrownBy {
            stayPlayer.hit()
        }.isInstanceOf(RuntimeException::class.java)
            .hasMessageContaining("Invalid state transition")
    }

    "Hand 의 sum 이 21인 Player 는 blackjack 상태가 된다" {
        val hand = Hand(
            HandCards(
                mutableListOf(
                    Card(Suit.Spade, Character.Jack),
                    Card(Suit.Clover, Character.Ace),
                )
            )
        )
        val player = Player("aaa", hand)

        player.addCard(Card(Suit.Diamond, Character.Jack))

        hand.valueSum() shouldBe 21
        hand.isBlackjack() shouldBe true
        hand.isBust() shouldBe false
        player.state shouldBe PlayerState.Blackjack
    }

    "Hand 의 sum 이 21을 초과하는 Player 는 bust 상태가 된다" {
        val hand = Hand(
            HandCards(
                mutableListOf(
                    Card(Suit.Spade, Character.Two),
                    Card(Suit.Clover, Character.Jack),
                )
            )
        )

        val player = Player("aaa", hand)

        player.addCard(Card(Suit.Diamond, Character.Jack))

        hand.valueSum() shouldBe 22
        hand.isBlackjack() shouldBe false
        hand.isBust() shouldBe true
        player.state shouldBe PlayerState.Bust
    }
})
