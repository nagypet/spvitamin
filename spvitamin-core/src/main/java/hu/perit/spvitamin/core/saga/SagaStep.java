/*
 * Copyright 2020-2025 the original author or authors.
 */

package hu.perit.spvitamin.core.saga;

public interface SagaStep<P, C>
{
    /**
     * A lokálisan perzisztált állapot szerint kész-e már ez a lépés? (gyors skip)
     */
    boolean isCompleted(C context);

    /**
     * Query-first helyreállítás: lekérdezi a külső rendszert, hogy a mellékhatás
     * megtörtént-e már. Ha igen, frissíti a contextet és true-t ad vissza.
     * (Lost-write védelem: "legenerálta, de nem mentettük el" eset.)
     */
    default boolean reconcile(P parameter, C context)
    {
        return false;
    }

    /**
     * A tényleges mellékhatás. Frissíti a contextet a részeredménnyel.
     */
    void execute(P parameter, C context) throws Exception;

    /**
     * Opcionális kompenzáció: ha egy rákövetkező lépés véglegesen hibásodik, ez a metódus
     * visszavonja az ebben a lépésben elvégzett mellékhatást.
     */
    default void compensate(P parameter, C context)
    {
    }
}
