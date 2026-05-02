object WaveSchedule {

    fun apply(spawner: EnemySpawner) {

        val next_wave = 60.0
        val next_wave2 = 120.0
        val next_wave3 = 180.0

        spawner.schedule(

            // =========================
            // WAVE 1 — Learning Phase
            // =========================
            SpawnEvent(1.0, "skeleton", 900.0),
            SpawnEvent(5.0, "skeleton", 1100.0),

            SpawnEvent(10.0, "skeleton", 900.0),
            SpawnEvent(12.0, "skeleton_archer", 1000.0),

            SpawnEvent(18.0, "skeleton", 1000.0, 2, 50.0),
            SpawnEvent(20.0, "skeleton_archer", 1000.0),

            SpawnEvent(26.0, "skeleton_spearman", 950.0),

            SpawnEvent(35.0, "skeleton", 1000.0),

            SpawnEvent(40.0, "skeleton", 1000.0, 2, 40.0),
            SpawnEvent(40.0, "skeleton_archer", 1000.0),

            SpawnEvent(50.0, "skeleton_spearman", 900.0),
            SpawnEvent(50.0, "skeleton_archer", 1000.0),
            SpawnEvent(50.0, "skeleton", 1000.0),

            SpawnEvent(60.0, "skeleton_boss", 640.0),

            // =========================
            // WAVE 1.1 — Starting Phase
            // =========================
            SpawnEvent(next_wave + 1.0, "skeleton", 900.0),
            SpawnEvent(next_wave + 5.0, "skeleton", 1100.0),

            SpawnEvent(next_wave + 10.0, "skeleton", 900.0),
            SpawnEvent(next_wave + 12.0, "skeleton_archer", 1000.0),

            SpawnEvent(next_wave + 18.0, "skeleton", 1000.0, 2, 50.0),
            SpawnEvent(next_wave + 20.0, "skeleton_archer", 1000.0),

            SpawnEvent(next_wave + 26.0, "skeleton_spearman", 950.0),

            SpawnEvent(next_wave + 35.0, "skeleton", 1000.0),

            SpawnEvent(next_wave + 40.0, "skeleton", 1000.0, 2, 40.0),
            SpawnEvent(next_wave + 40.0, "skeleton_archer", 1000.0),

            SpawnEvent(next_wave + 50.0, "skeleton_spearman", 900.0),
            SpawnEvent(next_wave + 50.0, "skeleton_archer", 1000.0),
            SpawnEvent(next_wave + 50.0, "skeleton", 1000.0),

            SpawnEvent(next_wave + 60.0, "skeleton_boss", 640.0),

            // =========================
            // WAVE 2 — Pressure Phase
            // =========================
            SpawnEvent(next_wave2 + 70.0, "skeleton", 1000.0, 2, 20.0),

            SpawnEvent(next_wave2 + 75.0, "wolf1", 950.0),

            SpawnEvent(next_wave2 + 80.0, "skeleton_archer", 1000.0, 2, 40.0),
            SpawnEvent(next_wave2 + 82.0, "skeleton", 1000.0, 2, 20.0),

            SpawnEvent(next_wave2 + 90.0, "skeleton_spearman", 1000.0),
            SpawnEvent(next_wave2 + 90.0, "wolf1", 950.0),

            SpawnEvent(next_wave2 + 100.0, "skeleton", 1000.0),

            SpawnEvent(next_wave2 + 105.0, "skeleton_archer", 1000.0, 2, 40.0),
            SpawnEvent(next_wave2 + 105.0, "wolf1", 900.0),

            SpawnEvent(next_wave2 + 110.0, "skeleton_spearman", 950.0),
            SpawnEvent(next_wave2 + 110.0, "skeleton", 1000.0, 2, 20.0),

            SpawnEvent(next_wave2 + 118.0, "skeleton", 1000.0),

            SpawnEvent(next_wave2 + 120.0, "skeleton_boss", 640.0 * 1.2),

            // =========================
            // WAVE 3 — Endgame
            // =========================
            SpawnEvent(next_wave3 + 130.0, "wolf1", 900.0),
            SpawnEvent(next_wave3 + 132.0, "skeleton_archer", 1000.0),

            SpawnEvent(next_wave3 + 140.0, "skeleton", 1000.0, 2, 20.0),
            SpawnEvent(next_wave3 + 140.0, "wolf1", 900.0),
            SpawnEvent(next_wave3 + 140.0, "skeleton_archer", 1000.0),

            SpawnEvent(next_wave3 + 150.0, "skeleton_spearman", 950.0),
            SpawnEvent(next_wave3 + 150.0, "wolf2", 900.0),

            SpawnEvent(next_wave3 + 160.0, "skeleton_archer", 1000.0, 2, 30.0),
            SpawnEvent(next_wave3 + 160.0, "skeleton", 1000.0, 2, 20.0),

            SpawnEvent(next_wave3 + 170.0, "wolf3", 900.0),
            SpawnEvent(next_wave3 + 170.0, "skeleton_spearman", 950.0),

            SpawnEvent(next_wave3 + 180.0, "skeleton_boss", 640.0 * 1.4),
        )
    }
}
