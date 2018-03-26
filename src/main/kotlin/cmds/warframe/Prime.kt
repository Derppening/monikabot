package cmds.warframe

import cmds.IBase
import cmds.Warframe.toNearestChronoYear
import core.BuilderHelper
import core.BuilderHelper.buildMessage
import core.BuilderHelper.insertSeparator
import core.ILogger
import core.Parser
import sx.blah.discord.handle.impl.events.guild.channel.message.MessageReceivedEvent
import java.io.File
import java.time.Duration
import java.time.Instant
import java.time.temporal.ChronoUnit

object Prime : IBase, ILogger {
    override fun handler(event: MessageReceivedEvent): Parser.HandleState {
        val args = getArgumentList(event.message.content).drop(1)

        if (args.any { it.matches(Regex("-{0,2}help")) }) {
            help(event, false)
            return Parser.HandleState.HANDLED
        } else if (args.size > 1) {
            help(event, false)
            return Parser.HandleState.HANDLED
        }

        displayPrimes(args, event)

        return Parser.HandleState.HANDLED
    }

    override fun help(event: MessageReceivedEvent, isSu: Boolean) {
        BuilderHelper.buildEmbed(event.channel) {
            withTitle("Help Text for `warframe-prime`")
            withDesc("Displays the most recently released primes, as well as predicts the next few primes.")
            insertSeparator()
            appendField("Usage", "```warframe primes [num_to_show]```", false)
            appendField("`[num_to_show]", "Number of released/predicted primes to show.", false)

            onDiscordError { e ->
                log(ILogger.LogLevel.ERROR, "Cannot display help text") {
                    author { event.author }
                    channel { event.channel }
                    info { e.errorMessage }
                }
            }
        }
    }

    private fun displayPrimes(args: List<String>, event: MessageReceivedEvent) {
        val listSize = if (args.isNotEmpty()) {
            try {
                args[0].toInt()
            } catch (e: NumberFormatException) {
                buildMessage(event.channel) {
                    withContent("The number of primes to show is not an integer!")
                }
                return
            }
        } else {
            5
        }

        val all = readFromFile()
        val primes = all.filter { it.primeDate != null }.sortedBy { it.primeDate?.epochSecond ?: 0 }.takeLast(listSize)
        val nonprimes = all.filter { it.primeDate == null }.sortedBy { it.date?.epochSecond ?: 0 }.take(listSize)

        buildMessage(event.channel) {
            appendContent("Released Primes:")
            primes.forEachIndexed { i, s ->
                appendContent("\n\t- ${s.name}")
                if (i != primes.size - 1 && s.name != "Excalibur") {
                    val duration = Duration.between(s.primeDate, primes[i + 1].primeDate).toDays()
                    appendContent(" (Lasted for $duration days)")
                }
            }
        }
        buildMessage(event.channel) {
            var time = primes.last().primeDate ?: error("Primes should have a prime date.")
            val male = nonprimes.filter { it.gender.toUpperCase() == 'M' }.sortedBy { it.date?.epochSecond ?: 0 }.toMutableList()
            val female = nonprimes.filter { it.gender.toUpperCase() == 'F' }.sortedBy { it.date?.epochSecond ?: 0 }.toMutableList()

            appendContent("**[PREDICTED]** Upcoming Primes:")
            val currentPrimes = primes.subList(primes.size - 2, primes.size).toMutableList()
            while (male.isNotEmpty() || female.isNotEmpty()) {
                time = time.plus(90, ChronoUnit.DAYS)
                when (currentPrimes[currentPrimes.size - 2].gender.toUpperCase()) {
                    'M' -> {
                        if (female.isNotEmpty()) {
                            currentPrimes.add(female[0])
                            female.removeAt(0)
                        } else {
                            currentPrimes.add(male[0])
                            male.removeAt(0)
                        }
                    }
                    'F' -> {
                        if (male.isNotEmpty()) {
                            currentPrimes.add(male[0])
                            male.removeAt(0)
                        } else {
                            currentPrimes.add(female[0])
                            female.removeAt(0)
                        }
                    }
                }
                val durationToPrime = Duration.between(Instant.now(), time)
                val durationStr = durationToPrime.toNearestChronoYear()
                appendContent("\n\t- ${currentPrimes.last().name} (In ~$durationStr)")
            }
        }
    }

    private fun readFromFile(): List<Prime> {
        val lines = File(Thread.currentThread().contextClassLoader.getResource(primeFilePath).toURI()).readLines().drop(1)
        val data = mutableListOf<Prime>()

        for (line in lines) {
            val props = line.split(',')
            check(props.size == 4)
            data.add(Prime(props[0], props[1][0], props[2].toLongOrNull() ?: 0L, props[3].toLongOrNull() ?: 0))
        }

        return data.toList()
    }

    class Prime(val name: String, val gender: Char, longDate: Long, longPrimeDate: Long) {
        private val _date: Instant = Instant.ofEpochSecond(longDate)
        private val _primeDate: Instant = Instant.ofEpochSecond(longPrimeDate)

        val date: Instant?
            get() = if (_date == Instant.EPOCH) {
                null
            } else {
                _date
            }

        val primeDate: Instant?
            get() = if (_primeDate == Instant.EPOCH) {
                null
            } else {
                _primeDate
            }
    }

    private const val primeFilePath = "data/primes.csv"
}