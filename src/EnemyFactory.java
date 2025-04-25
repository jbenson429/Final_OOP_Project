public class EnemyFactory {
    public static Enemy createEnemy(String type, int x, int y, Hero hero) {
        return switch (type.toLowerCase()) {
            case "swooping" -> new SwoopingEnemy(x, y, hero);
            //case "shooting" -> new ShootingEnemy(x, y);
            //case "capturing" -> new CapturingEnemy(x, y);
            default -> throw new IllegalArgumentException("Unknown enemy type: " + type);
        };
    }
}
