import java.util.*
import java.lang.Math.sqrt

const val HEIGHT = 1000
const val WIDTH = 1920

enum class BuildType {
    EMPTY, TOWER, KNIGHT, ARCHER, GIANT;

    fun isBarracks(): Boolean {
        return this != EMPTY && this != TOWER
    }
}

class Site(val id: Int, x: Int, y: Int, r: Int,
           var coolDown: Int = Int.MAX_VALUE,
           var attackR: Int = 0,
           var isTouch: Boolean = false,
           var isMy: Boolean = false,
           var type: BuildType = BuildType.EMPTY) : GameObject(x, y, r)

open class GameObject(var x: Int, var y: Int, val r: Int)
open class GameUnit(x: Int, y: Int, r: Int, var health: Int) : GameObject(x, y, r)
class Queen(x: Int, y: Int, health: Int): GameUnit(x, y, 30, health)

fun main(args : Array<String>) {

    val input = Scanner(System.`in`)
    val numSites = input.nextInt()
    val sites = mutableListOf<Site>()
    for (i in 0 until numSites) {
        val siteId = input.nextInt()
        val x = input.nextInt()
        val y = input.nextInt()
        val radius = input.nextInt()

        sites.add(Site(siteId, x, y, radius))
    }

    val siteMap = sites.associateBy({it.id}, {it})
    val queen = Queen(0,0,100)

    while (true) {
        siteMap.forEach { _, site -> site.isTouch = false }

        val gold = input.nextInt()
        val touchedSite = input.nextInt()
        siteMap[touchedSite]?.isTouch = true
        for (i in 0 until numSites) {
            val siteId = input.nextInt()
            val ignore1 = input.nextInt() // used in future leagues
            val ignore2 = input.nextInt() // used in future leagues
            val structureType = input.nextInt() // -1 = No structure, 2 = Barracks
            val owner = input.nextInt() // -1 = No structure, 0 = Friendly, 1 = Enemy
            val param1 = input.nextInt()
            val param2 = input.nextInt()

            siteMap[siteId]?.coolDown = param1
            siteMap[siteId]?.isMy = owner == 0

            when (structureType) {
                1 -> {
                    siteMap[siteId]?.type = BuildType.TOWER
                    siteMap[siteId]?.attackR = param2
                }
                2 -> {
                    siteMap[siteId]?.type = when (param2) {
                        0 -> BuildType.KNIGHT
                        1 -> BuildType.ARCHER
                        else -> BuildType.GIANT
                    }
                }
                else -> siteMap[siteId]?.type = BuildType.EMPTY
            }
        }
        val numUnits = input.nextInt()
        for (i in 0 until numUnits) {
            val x = input.nextInt()
            val y = input.nextInt()
            val owner = input.nextInt()
            val unitType = input.nextInt() // -1 = QUEEN, 0 = KNIGHT, 1 = ARCHER
            val health = input.nextInt()

            if (owner == 0) {
                when (unitType) {
                    -1 -> {
                        queen.x = x
                        queen.y = y
                        queen.health = health
                    }
                }
            }
        }

        val nearestEmptySite = siteMap
                .filter { it.value.type == BuildType.EMPTY && !it.value.isTouch }
                .mapValues { dist(queen, it.value) }
                .minBy { it.value }


        val touched = siteMap.filter { it.value.isTouch && it.value.type == BuildType.EMPTY }.keys.firstOrNull()

        when {
            touched != null -> println("")
            nearestEmptySite != null -> {
                val targetSite = siteMap[nearestEmptySite.key]
                println("MOVE ${targetSite?.x} ${targetSite?.y}")
            }
            else -> println("WAIT")
        }

        val training = siteMap.values
                .filter { it.type.isBarracks() && it.coolDown == 0 }
                .takeLast(gold / 80)
                .joinToString(separator = " ") { it.id.toString() }

        val trainResult = if (training.isBlank()) {
            "TRAIN"
        } else {
            "TRAIN $training"
        }

        println(trainResult)
    }
}

fun dist(from: GameObject, to: GameObject): Double {
    val dx = (to.x - from.x).toDouble()
    val dy = (to.y - from.y).toDouble()
    return sqrt(dx*dx + dy*dy)
}

fun build(siteIt: Int, type: BuildType): String {
    return when (type) {
        BuildType.TOWER -> "BUILD $siteIt TOWER"
        else -> "BUILD $siteIt BARRACKS-${type.name}"
    }
}