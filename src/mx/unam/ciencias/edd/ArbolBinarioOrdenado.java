package mx.unam.ciencias.edd;

import java.util.Iterator;

import java.util.NoSuchElementException;


/**
 * <p>Clase para árboles binarios ordenados. Los árboles son genéricos, pero
 * acotados a la interfaz {@link Comparable}.</p>
 *
 * <p>Un árbol instancia de esta clase siempre cumple que:</p>
 * <ul>
 *   <li>Cualquier elemento en el árbol es mayor o igual que todos sus
 *       descendientes por la izquierda.</li>
 *   <li>Cualquier elemento en el árbol es menor o igual que todos sus
 *       descendientes por la derecha.</li>
 * </ul>
 */
public class ArbolBinarioOrdenado<T extends Comparable<T>>
    extends ArbolBinario<T> {

    /* Clase privada para iteradores de árboles binarios ordenados. */
    private class Iterador implements Iterator<T> {

        /* Pila para recorrer los vértices en DFS in-order. */
        private Pila<Vertice> pila;

        /* Construye un iterador con el vértice recibido. */
        public Iterador() {
            pila = new Pila<Vertice>();
            if (raiz != null) {
                pila.mete(raiz);
                Vertice v = raiz;
                while (v.izquierdo != null) {
                    pila.mete(v.izquierdo);
                    v = v.izquierdo;
                }
            }
        }

        /* Nos dice si hay un elemento siguiente. */
        @Override public boolean hasNext() {
            return (!(pila.esVacia()));
        }

        /* Regresa el siguiente elemento en orden DFS in-order. */
        @Override public T next() {
            Vertice v = pila.saca();
            if (v.derecho != null) {
                pila.mete(v.derecho);
                Vertice v2 = v.derecho;
                while(v2.izquierdo != null) {
                    pila.mete(v2.izquierdo);
                    v2 = v2.izquierdo;
                }
            }
            return v.elemento;
        }
    }

    /**
     * El vértice del último elemento agegado. Este vértice sólo se puede
     * garantizar que existe <em>inmediatamente</em> después de haber agregado
     * un elemento al árbol. Si cualquier operación distinta a agregar sobre el
     * árbol se ejecuta después de haber agregado un elemento, el estado de esta
     * variable es indefinido.
     */
    protected Vertice ultimoAgregado;

    /**
     * Constructor sin parámetros. Para no perder el constructor sin parámetros
     * de {@link ArbolBinario}.
     */
    public ArbolBinarioOrdenado() { super(); }

    /**
     * Construye un árbol binario ordenado a partir de una colección. El árbol
     * binario ordenado tiene los mismos elementos que la colección recibida.
     * @param coleccion la colección a partir de la cual creamos el árbol
     *        binario ordenado.
     */
    public ArbolBinarioOrdenado(Coleccion<T> coleccion) {
        super(coleccion);
    }

    /**
     * Agrega un nuevo elemento al árbol. El árbol conserva su orden in-order.
     * @param elemento el elemento a agregar.
     */
    @Override public void agrega(T elemento) {
        if (elemento == null)
            throw new IllegalArgumentException();
        Vertice nuevo = nuevoVertice(elemento);
        ultimoAgregado = nuevo;
        if (++elementos == 1)
            raiz = nuevo;
        else
            agrega(raiz, nuevo);
    }

    private void agrega(Vertice vertice, Vertice nuevo) {
        if (nuevo.elemento.compareTo(vertice.elemento) >= 0)
            if (vertice.derecho != null)
                agrega(vertice.derecho, nuevo);
            else {
                vertice.derecho = nuevo;
                nuevo.padre = vertice;
                return;
            }
        else
            if (vertice.izquierdo != null)
                agrega(vertice.izquierdo, nuevo);
            else {
                vertice.izquierdo = nuevo;
                nuevo.padre = vertice;
                return;
            }
    }

    /**
     * Elimina un elemento. Si el elemento no está en el árbol, no hace nada; si
     * está varias veces, elimina el primero que encuentre (in-order). El árbol
     * conserva su orden in-order.
     * @param elemento el elemento a eliminar.
     */
    @Override public void elimina(T elemento) {
        Vertice v = vertice(busca(elemento));
        if (v != null) {
            elementos--;
            if (v.izquierdo != null && v.derecho != null)
                v = intercambiaEliminable(v);
            eliminaVertice(v);
        }
    }

    protected Vertice maxSubArbol(Vertice v) {
        if (v.derecho == null)
            return v;
        return maxSubArbol(v.derecho);
    }

    /**
     * Intercambia el elemento de un vértice con dos hijos distintos de
     * <code>null</code> con el elemento de un descendiente que tenga a lo más
     * un hijo.
     * @param vertice un vértice con dos hijos distintos de <code>null</code>.
     * @return el vértice descendiente con el que vértice recibido se
     *         intercambió. El vértice regresado tiene a lo más un hijo distinto
     *         de <code>null</code>.
     */
    protected Vertice intercambiaEliminable(Vertice vertice) {
        Vertice max = maxSubArbol(vertice.izquierdo);
        T e = vertice.elemento;
        vertice.elemento = max.elemento;
        max.elemento = e;
        return max;
    }

    /**
     * Elimina un vértice que a lo más tiene un hijo distinto de
     * <code>null</code> subiendo ese hijo (si existe).
     * @param vertice el vértice a eliminar; debe tener a lo más un hijo
     *                distinto de <code>null</code>.
     */
    protected void eliminaVertice(Vertice vertice) {
        Vertice u = (vertice.izquierdo != null && vertice.derecho == null)
                ? vertice.izquierdo : vertice.derecho;
        if (u != null)
            u.padre = vertice.padre;
        if (vertice.padre != null)
            if (vertice.padre.izquierdo != null && vertice.padre.izquierdo == vertice)
                vertice.padre.izquierdo = u;
            else
                vertice.padre.derecho = u;
        else
            raiz = u;
    }

    /**
     * Busca un elemento en el árbol recorriéndolo in-order. Si lo encuentra,
     * regresa el vértice que lo contiene; si no, regresa <tt>null</tt>.
     * @param elemento el elemento a buscar.
     * @return un vértice que contiene al elemento buscado si lo
     *         encuentra; <tt>null</tt> en otro caso.
     */
    @Override public VerticeArbolBinario<T> busca(T elemento) {
        if (elemento == null || raiz == null)
            return null;
        return busca(elemento, raiz);
    }

    private VerticeArbolBinario<T> busca(T elemento, VerticeArbolBinario<T> vertice) {
        if (elemento.compareTo(vertice.get()) == 0)
            return vertice;
        else if (elemento.compareTo(vertice.get()) > 0 && vertice.hayDerecho())
            return busca(elemento, vertice.derecho());
        else if (vertice.hayIzquierdo())
            return busca(elemento, vertice.izquierdo());
        return null;
    }

    /**
     * Regresa el vértice que contiene el último elemento agregado al
     * árbol. Este método sólo se puede garantizar que funcione
     * <em>inmediatamente</em> después de haber invocado al método {@link
     * agrega}. Si cualquier operación distinta a agregar sobre el árbol se
     * ejecuta después de haber agregado un elemento, el comportamiento de este
     * método es indefinido.
     * @return el vértice que contiene el último elemento agregado al árbol, si
     *         el método es invocado inmediatamente después de agregar un
     *         elemento al árbol.
     */
    public VerticeArbolBinario<T> getUltimoVerticeAgregado() {
        return ultimoAgregado;
    }

    /**
     * Gira el árbol a la derecha sobre el vértice recibido. Si el vértice no
     * tiene hijo izquierdo, el método no hace nada.
     * @param vertice el vértice sobre el que vamos a girar.
     */
    public void giraDerecha(VerticeArbolBinario<T> vertice) {
        Vertice q = vertice(vertice);
        if (q.izquierdo != null) {
            Vertice p = q.izquierdo;
            p.padre = q.padre;
            if (q.padre != null)
                if (q.padre.izquierdo != null && q.padre.izquierdo == q)
                    q.padre.izquierdo = p;
                else
                    q.padre.derecho = p;
            else
                raiz = p;
            q.padre = p;
            q.izquierdo = p.derecho;
            if (p.derecho != null)
                p.derecho.padre = q;
            p.derecho = q;
        }
    }

    /**
     * Gira el árbol a la izquierda sobre el vértice recibido. Si el vértice no
     * tiene hijo derecho, el método no hace nada.
     * @param vertice el vértice sobre el que vamos a girar.
     */
    public void giraIzquierda(VerticeArbolBinario<T> vertice) {
        Vertice p = vertice(vertice);
        if (p.derecho != null) {
            Vertice q = p.derecho;
            q.padre = p.padre;
            if (p.padre != null) {
                if (p.padre.izquierdo != null && p.padre.izquierdo == p)
                    p.padre.izquierdo = q;
                else
                    p.padre.derecho = q;
            }
            else
                raiz = q;
            p.padre = q;
            p.derecho = q.izquierdo;
            if (q.izquierdo != null)
                q.izquierdo.padre = p;
            q.izquierdo = p;
        }
    }

    /**
     * Realiza un recorrido DFS <em>pre-order</em> en el árbol, ejecutando la
     * acción recibida en cada elemento del árbol.
     * @param accion la acción a realizar en cada elemento del árbol.
     */
    public void dfsPreOrder(AccionVerticeArbolBinario<T> accion) {
        dfsPreOrder(accion, raiz);
    }

    private void dfsPreOrder(AccionVerticeArbolBinario<T> accion, Vertice v) {
        if (v == null)
            return;
        accion.actua(v);
        dfsPreOrder(accion, v.izquierdo);
        dfsPreOrder(accion, v.derecho);
    }

    /**
     * Realiza un recorrido DFS <em>in-order</em> en el árbol, ejecutando la
     * acción recibida en cada elemento del árbol.
     * @param accion la acción a realizar en cada elemento del árbol.
     */
    public void dfsInOrder(AccionVerticeArbolBinario<T> accion) {
        dfsInOrder(accion, raiz);
    }

    private void dfsInOrder(AccionVerticeArbolBinario<T> accion, Vertice v) {
        if (v == null)
            return;
        dfsInOrder(accion, v.izquierdo);
        accion.actua(v);
        dfsInOrder(accion, v.derecho);
    }

    /**
     * Realiza un recorrido DFS <em>post-order</em> en el árbol, ejecutando la
     * acción recibida en cada elemento del árbol.
     * @param accion la acción a realizar en cada elemento del árbol.
     */
    public void dfsPostOrder(AccionVerticeArbolBinario<T> accion) {
        dfsPostOrder(accion, raiz);
    }

    private void dfsPostOrder(AccionVerticeArbolBinario<T> accion, Vertice v) {
        if (v == null)
            return;
        dfsPostOrder(accion, v.izquierdo);
        dfsPostOrder(accion, v.derecho);
        accion.actua(v);
    }

    /**
     * Regresa un iterador para iterar el árbol. El árbol se itera en orden.
     * @return un iterador para iterar el árbol.
     */
    @Override public Iterator<T> iterator() {
        return new Iterador();
    }
}
