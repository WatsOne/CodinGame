import java.util.*
import java.io.*
import java.lang.Math.sqrt

const val HEIGHT = 1000
const val WIDTH = 1920

enum class SiteType {
    EMPTY, BARRACKS, ENEMY_BARRACKS
}

class Site(val id: Int, x: Int, y: Int, r: Int,
           var cooldown: Int = Int.MAX_VALUE,
           var isTouch: Boolean = false,
           var type: SiteType = SiteType.EMPTY) : GameObject(x, y, r)

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

            siteMap[siteId]?.cooldown = param1
            if (owner == 0) {
                siteMap[siteId]?.type = when (structureType) {
                    2 -> SiteType.BARRACKS
                    else -> SiteType.EMPTY
                }
            } else if (owner == 1) {
                siteMap[siteId]?.type = when (structureType) {
                    2 -> SiteType.ENEMY_BARRACKS
                    else -> SiteType.EMPTY
                }
            }

        }
        val numUnits = input.nextInt()
        for (i in 0 until numUnits) {
            val x = input.nextInt()
            val y = input.nextInt()
            val owner = input.nextInt()
            val unitType = input.nextInt() // -1 = QUEEN, 0 = KNIGHT, 1 = ARCHER
            val health = input.nextInt()

            if (owner == 1) {
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
                .filter { it.value.type == SiteType.EMPTY && !it.value.isTouch }
                .mapValues { dist(queen, it.value) }
                .minBy { it.value }


        val touched = siteMap.filter { it.value.isTouch }[0]

        when {
            touched != null -> println("BUILD ${touched.id} BARRACKS-KNIGHT")
            nearestEmptySite != null -> {
                val targetSite = siteMap[nearestEmptySite.key]
                println("MOVE ${targetSite?.x} ${targetSite?.y}")
            }
            else -> println("WAIT")
        }

        val training = siteMap.values
                .filter { it.type == SiteType.BARRACKS && it.cooldown == 0 }
                .map { it.id.toString() }
                .joinToString { " " }

        println("TRAIN $training")
    }
}

fun dist(from: GameObject, to: GameObject): Double {
    val dx = (to.x - from.x).toDouble()
    val dy = (to.y - from.y).toDouble()
    return sqrt(dx*dx + dy*dy)
}