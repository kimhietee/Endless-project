object WaveSchedule {

    fun apply(spawner: EnemySpawner) {

        spawner.schedule(

            // =========================
            // WAVE 1 — Learning Phase
            // =========================
            SpawnEvent(1.0, "skeleton", 1000.0),
            SpawnEvent(5.0, "skeleton", 1000.0),

            SpawnEvent(10.0, "skeleton", 1000.0),
            SpawnEvent(12.0, "skeleton_archer", 1000.0),

            SpawnEvent(18.0, "skeleton", 1000.0, 2, 25.0),
            SpawnEvent(20.0, "skeleton_archer", 1000.0),

            SpawnEvent(26.0, "skeleton_spearman", 950.0),

            SpawnEvent(35.0, "skeleton", 1000.0),

            SpawnEvent(40.0, "skeleton", 1000.0, 2, 20.0),
            SpawnEvent(40.0, "skeleton_archer", 1000.0),

            SpawnEvent(50.0, "skeleton_spearman", 900.0),
            SpawnEvent(50.0, "skeleton_archer", 1000.0),

            SpawnEvent(55.0, "skeleton", 1000.0),

            SpawnEvent(60.0, "skeleton_boss", 640.0),

            // =========================
            // WAVE 2 — Pressure Phase
            // =========================
            SpawnEvent(70.0, "skeleton", 1000.0, 2, 20.0),

            SpawnEvent(75.0, "wolf1", 950.0),

            SpawnEvent(80.0, "skeleton_archer", 1000.0, 2, 40.0),
            SpawnEvent(82.0, "skeleton", 1000.0, 2, 20.0),

            SpawnEvent(90.0, "skeleton_spearman", 1000.0),
            SpawnEvent(90.0, "wolf1", 950.0),

            SpawnEvent(100.0, "skeleton", 1000.0),

            SpawnEvent(105.0, "skeleton_archer", 1000.0, 2, 40.0),
            SpawnEvent(105.0, "wolf1", 900.0),

            SpawnEvent(110.0, "skeleton_spearman", 950.0),
            SpawnEvent(110.0, "skeleton", 1000.0, 2, 20.0),

            SpawnEvent(118.0, "skeleton", 1000.0),

            SpawnEvent(120.0, "skeleton_boss", 640.0 * 1.2),

            // =========================
            // WAVE 3 — Endgame
            // =========================
            SpawnEvent(130.0, "wolf1", 900.0),
            SpawnEvent(132.0, "skeleton_archer", 1000.0),

            SpawnEvent(140.0, "skeleton", 1000.0, 2, 20.0),
            SpawnEvent(140.0, "wolf1", 900.0),
            SpawnEvent(140.0, "skeleton_archer", 1000.0),

            SpawnEvent(150.0, "skeleton_spearman", 950.0),
            SpawnEvent(150.0, "wolf2", 900.0),

            SpawnEvent(160.0, "skeleton_archer", 1000.0, 2, 30.0),
            SpawnEvent(160.0, "skeleton", 1000.0, 2, 20.0),

            SpawnEvent(170.0, "wolf3", 900.0),
            SpawnEvent(170.0, "skeleton_spearman", 950.0),

            SpawnEvent(180.0, "skeleton_boss", 640.0 * 1.4),
        )
    }
}
