
# Formation Spring Batch

Le projet lit un fichier CSV avec Spring batch, mets les lignes non dupliqué et filtré dans une table SQLite. Des informations sont remontées avec Micrometer et spring batch. Les données de la table sont disponible grâce à l'API Swagger.






## Lancement
Lancement du projet, pas besoin de paramètre. Si ajout de paramètre pour filtrer avec le code postale, il faut modifier le BanApplication, deux paramètres code_Postal et CodePostalFilter doivent être changé.

Lancement de la page Swagger: http://localhost:8081/swagger-ui/index.html#/


