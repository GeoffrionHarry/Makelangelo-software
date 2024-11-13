

"-Xms512m -Xmx1024m"

Ayant testé l'exécution de la GitHub Action avec ce paramètre, nous confirmons que cette allocation mémoire est suffisante pour rouler la séquence de tests unitaire. Alors, en utilisant le flag Xms512m pour fixer le minimum de mémoire, nous sommes sure que la séquence de tests ne sera pas interrompu par des "OutOfMemoryErrors". De plus, la limite fixer par Xmx1024m assure que l'application ne prendre pas plus de 1GB de mémoire ce qui pourrait prévenir des conflits de quantités de mémoires possibles avec d'autres applications. Le tout pourrait aussi avoir un impact sur la performance des tests par optimisation de la "garbage collection".


"-XX:+TieredCompilation"

Ce flag optimise la compilation de l'application en utilisant d'abord un compilateur "C1" qui s'occupe d'abord de partir la séquence de tests et qui passe ensuite à un deuxième compilateur "C2" qui utilise les données d'utilisation de l'application pour optimiser les parties moins performantes. Ce flag permet donc un départ rapide des tests tout en continuant à optimiser le tout pour accélérer le reste de la séquence de tests.


"-XX:+UseParallelGC" 

Ce flag dit à la JVM d'utiliser plusieurs "threads" lors de la "garbage collection" pour rendre cet étape plus vite. Ceci accélére surtout les applications qui utilise un grand nombre d'objet qui vivent peu de temps en mémoire tel que le fait Makelangelo, surtout dans le cas des tests unitaires. Bref, ce flag améliore la performance de l'application.


"-XX:+PrintGCDetails"

Ce flag vise à augmenter l'observabilité de l'application en donnant le compte rendu de la mémoire et du processus de "garbage collection" de l'application à chaque fois fois que ce processus est effectué. En ayant une meilleur idée de l'état de la mémoire et de son utilisation, les développeurs peuvent optimiser l'utilisation mémoire de l'application ainsi que possiblement trouver des bugs.


"-XX:+UseCompressedOops"

Le dernier flag sert à réduire l'adresse mémoire des objets de 64 bit à 32 bits dans l'application. En pratique, j'ai observé qu'avec ce flag, la majorité de la mémoire était dans la "young generation" à la fin de la séquence de tests ce qui indique que le garbage collector n'a pas eu à faire beaucoup d'étape de garbage collection eet que la mémoire disponible dans le heap était suffisante au cours de l'utiliation de l'application. À l'inverse, sans ce flag, l'utilisation mémoire de la "old generation" était beaucoup plus élevée. Ceci a comme effet d'augmenter la performance de l'application puisqu'on a eu moins d'étapes de "garbage collection" à faire et aussi d'améliorer l'utilisation mémoire de l'application lui permettant d'utiliser moins de mémoire pour effectuer la séquence de tests unitaires.