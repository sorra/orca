# Orca
ORCA (Object Relational CApability) is an ORM framework for Java.
## Motivation
Why another framework, why not Hibernate (i.e. JPA)?

These are some of the advantages that inspire us to build Orca:
1. Orca supports low-code application development.
2. Hibernate is a stateful framework so you need to access data in the scope of an open session (i.e. PersistenceContext). Orca is a stateless framework so you can use it more easily.
3. Orca solves some performance issues of Hibernate which are mostly related to Hibernates' stateful session.
## Project Status
This project is in an early stage. We are actively working to make it production ready. MySQL and PostgreSQL will be supported first.

Currently, its features are proven by unit tests. In brief, it supports entity metadata definition, entity class generation, DB schema generation, CRUD, associations (everything: one-to-one, one-to-many, many-to-one, many-to-many), cascading, and finding by object navigation path. Especially, it can handle circular associations (e.g. A->A and A->B->A).

You can look at https://github.com/sorra/orca/tree/main/src/test to check what things it can do.
