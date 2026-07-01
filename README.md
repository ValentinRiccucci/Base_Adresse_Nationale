
# Formation Spring Batch

Le projet lit un fichier CSV avec Spring Batch, met les lignes non dupliquées et filtrées dans une table SQLite. Des informations sont remontées avec Micrometer et Spring Batch. Les données de la table sont disponibles grâce à l'API Swagger.






## Lancement
Lancement du projet, pas besoin de paramètre. Si ajout de paramètres pour filtrer avec le code postal, il faut modifier le BanApplication, deux paramètres, code_postal et CodePostalFilter, doivent être changés.

Lancement de la page Swagger: http://localhost:8081/swagger-ui/index.html#/


