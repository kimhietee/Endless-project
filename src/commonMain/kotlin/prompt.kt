//You are working inside an existing Kotlin KorGE project.
//
//==================================================
//STRICT RULES
//==================================================
//
//- DO NOT rewrite the system
//- DO NOT create new systems
//- DO NOT guess values
//- DO NOT modify working logic unnecessarily
//- If something is already correct, DO NOT change it
//
//Only fix incorrect frame counts and remove duplication IF it exists.
//
//==================================================
//MAIN GOAL
//==================================================
//
//Fix ALL enemy animation frame counts based on the EXACT data below.
//
//Also:
//- Ensure frame counts are defined in ONE place only (EnemyConfig)
//- If EnemyFactory also defines frame counts → REMOVE duplication there
//- I want to edit frame counts ONLY ONCE in the future
//
//If the system is already centralized:
//→ DO NOT refactor, only fix incorrect values
//
//==================================================
//STEP 1 — ANALYZE FIRST (REQUIRED)
//==================================================
//
//Before making changes:
//- Check where frame counts are defined
//- Check if EnemyFactory duplicates frame data
//
//Explain briefly BEFORE editing.
//
//==================================================
//STEP 2 — APPLY CORRECT FRAME DATA
//==================================================
//
//Use EXACT values below.
//
//------------------------
//SKELETON BOSS
//------------------------
//- attack: 1 row, 6 columns
//- projectile: 1 row, 8 columns
//
//------------------------
//SKELETON ARCHER
//------------------------
//- attack: 1 row, 15 columns
//- run: 1 row, 8 columns
//- death: 1 row, 5 columns
//
//------------------------
//SKELETON (NORMAL)
//------------------------
//- attack: 1 row, 6 columns
//- run: 1 row, 8 columns
//- death: 1 row, 4 columns
//
//------------------------
//SKELETON SPEARMAN
//------------------------
//- attack: 1 row, 4 columns
//- death: 1 row, 5 columns
//
//------------------------
//ATTACK EFFECTS
//------------------------
//- skeleton slash: 1 row, 4 columns
//- spear slash: 1 row, 5 columns
//
//------------------------
//PROJECTILE
//------------------------
//arrow:
//- SINGLE FRAME
//
//REQUIREMENT (MANDATORY):
//- Must NOT disappear instantly
//- Must remain visible long enough to deal damage
//- Fix by ONE of the following:
//  - loop animation
//  - increase frame duration
//  - OR control lifetime in code
//
//------------------------
//WOLVES
//------------------------
//
//Shared:
//- death: 1 row, 2 columns
//
//wolf1:
//- attack: 1 row, 7 columns
//- run: 1 row, 9 columns
//
//wolf2:
//- attack: 1 row, 4 columns
//
//wolf3:
//- attack: 1 row, 5 columns
//- run: 1 row, 11 columns
//
//------------------------
//WOLF ATTACK EFFECT
//------------------------
//- slash: 12 separate PNG frames (NOT spritesheet)
//
//Use frame sequence loading (NOT rows/columns)
//
//==================================================
//IMPLEMENTATION RULES
//==================================================
//
//- Use existing FrameConfig correctly:
//  - spritesheet → rows/columns
//  - PNG sequence → frame count
//
//- REMOVE incorrect defaults (example: count = 10)
//
//- DO NOT define frame counts in multiple places
//
//==================================================
//OUTPUT
//==================================================
//
//Provide:
//
//1. Analysis (where duplication exists)
//2. Corrected EnemyConfigs (only changed parts)
//3. EnemyFactory fixes (ONLY if duplication exists)
//4. Short explanation of what was fixed
//
//==================================================
//FAIL CONDITIONS
//==================================================
//
//Solution is WRONG if:
//- Frame data is ignored
//- Systems are rewritten
//- Frame counts exist in multiple places
//- Arrow disappears instantly
