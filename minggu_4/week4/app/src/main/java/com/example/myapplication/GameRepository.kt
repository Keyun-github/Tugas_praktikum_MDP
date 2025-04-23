package com.example.myapplication // <-- Sesuaikan package name

object GameRepository {
    val games = mutableListOf<Game>()

    init {
        games.add(
            Game("Monster Hunter Wilds", "The unbridled force of nature runs wild...", listOf("Action", "Multiplayer", "Co-op"), false, 859000)
        )
        games.add(
            Game("Cyberpunk 2077", "Open-world, action-adventure story set in Night City...", listOf("Open World", "RPG"), false, 699999)
        )
        games.add(
            Game("Counter-Strike 2", "An upgrade to CS:GO on the Source 2 engine.", listOf("FPS", "Shooter", "Multiplayer", "Competitive"), true, 0)
        )
        games.add(
            Game("Split Fiction", "A cool cooperative game.", listOf("Co-op"), false, 569000)
        )
        games.add(
            Game("Assassin's Creed Shadows", "Explore feudal Japan.", listOf("Action", "Open World"), false, 859000)
        )
    }
fun addGame(game: Game) {
        games.add(game)
    }

    fun findGameByName(name: String): Game? {
        return games.find { it.name == name }
    }
}