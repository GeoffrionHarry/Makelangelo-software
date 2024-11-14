# Documentation

#### Petite note:
Pour une raison que nous ignorons, quand nous avons commencé la tâche 3, nous avons cru qu'il fallait le faire dans un repo séparé de la tâche 2. Nous réalisons maintenant que ce n'est pas le cas puisque la couverture de code devrait augmenter quand la GitHub Action roule. Pour remédier à la situation, nous avons copié les fichiers de la tâche 2 dans ce repo. La couverture de code est donc augmentée comme il faut pour les tests.

#### Humour:
Travailler avec Java et la JVM au cours de ce projet nous a été super utile ! Nous avons appris beaucoup de choses, notamment qu'une carrière en Java n'est définitivement pas pour nous :p
(On niaise, promis!)


## Flags

#### Flag "-Xms512m -Xmx1024m"

Ayant testé l'exécution de la GitHub Action avec ce paramètre, nous confirmons que cette allocation mémoire est suffisante pour exécuter la séquence de tests unitaires. Ainsi, en utilisant le flag Xms512m pour fixer le minimum de mémoire, nous sommes sûrs que la séquence de tests ne sera pas interrompue par des "OutOfMemoryErrors". De plus, la limite fixée par Xmx1024m assure que l'application ne prendra pas plus de 1GB de mémoire, ce qui pourrait prévenir des conflits de quantités de mémoire possibles avec d'autres applications. Le tout pourrait également avoir un impact sur la performance des tests par l'optimisation de la "garbage collection".


#### Flag "-XX:+TieredCompilation"

Ce flag optimise la compilation de l'application en utilisant d'abord un compilateur "C1" qui s'occupe de lancer la séquence de tests, puis un deuxième compilateur "C2" qui utilise les données d'utilisation de l'application pour optimiser les parties moins performantes. Ce flag permet donc un départ rapide des tests tout en continuant à optimiser l'ensemble pour accélérer le reste de la séquence de tests.


#### Flag "-XX:+UseParallelGC" 

Ce flag indique à la JVM d'utiliser plusieurs "threads" lors de la "garbage collection" pour rendre cette étape plus rapide. Cela accélère surtout les applications qui utilisent un grand nombre d'objets à durée de vie courte en mémoire, comme le fait Makelangelo, particulièrement dans le cas des tests unitaires. En bref, ce flag améliore la performance de l'application.


#### Flag "-XX:+PrintGCDetails"

Ce flag vise à augmenter l'observabilité de l'application en fournissant un compte rendu de la mémoire et du processus de "garbage collection" de l'application à chaque fois que ce processus est effectué. En ayant une meilleure idée de l'état de la mémoire et de son utilisation, les développeurs peuvent optimiser l'utilisation mémoire de l'application et potentiellement trouver des bugs.


#### Flag "-XX:+UseCompressedOops"

Le dernier flag sert à réduire l'adresse mémoire des objets de 64 bits à 32 bits dans l'application. En pratique, nous avons observé qu'avec ce flag, la majorité de la mémoire était dans la "young generation" à la fin de la séquence de tests, ce qui indique que le garbage collector n'a pas eu à faire beaucoup d'étapes de garbage collection et que la mémoire disponible dans le heap était suffisante au cours de l'utilisation de l'application. À l'inverse, sans ce flag, l'utilisation mémoire de la "old generation" était beaucoup plus élevée. Ceci a pour effet d'augmenter la performance de l'application puisqu'on a eu moins d'étapes de "garbage collection" à faire et d'améliorer l'utilisation mémoire de l'application, lui permettant d'utiliser moins de mémoire pour effectuer la séquence de tests unitaires.