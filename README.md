# Arkadiko #

Spring's dependency injection framework is the picture of ubiquity in the java world. As such it aims to drive developers toward declarative, component driven design and lose coupling. But, it is largely a static container. The class space is flat and once started there is little means for dynamic change.

OSGi is "the" modularity framework for java. It supports dynamic change via strict classloader isolation and packaging metadata. It also aims to drive developers toward modular design. But although it is very powerfull it is also complex and learning it's intricacies can be daunting.

These two paradigms have recently undergone a merge or sorts in such that within OSGi frameworks one can use the declarative nature of Spring (via Spring DM and more recently standardised as Blueprint) to configure and wire together components as they come and go from the OSGi framework.

### The Problem ###

Given the sheer number of Spring based projects and the most recent up-surge in the desire to benefit from OSGi's dynamic modularity it has become clear that there can be significant difficulties in moving projects of any complexity to OSGi. The issue is the learning curve surrounding design changes that must be made in moving traditional java applications, including Spring based ones. OSGi has a puritanical solution to a vast number of problems caused by the traditional architectures, but in order to gain from those solutions a considerable amount of redesign has to be done.

### What to do? ###

There are several well known projects and individuals promoting methodologies and best practices to help undertake the considerable amount of work that such migrations potentially involve.

A well known presentation by BJ Hargrave (IBM) and Peter Kriens (aQute) (http://www.slideshare.net/bjhargrave/servicesfirst-migration-to-osgi) defines the concept of "services first" migration methodology. This methodology suggests that the first steps in the migration is to re-design the existing architecture such that it is based on a services oriented design. They offer some insight into how that is accomplished paraphrased over several different articles about μservices (micro-services). Once this has been accomplished it becomes far simpler to isolate portions of code that form logical components into modules which use or publish services in well defined manner to subsequently be turned into OSGi bundles.

Also, a core developer of the Apache Felix project, Karl Pauls, maintains a library called PojoSR (http://code.google.com/p/pojosr/) based on some of the Apache Felix project code that itself does not implement a full OSGi framework container, but essentially provides an active registry which scans the class space for recognizable "services" and effectively tries to wire those together and provides a central access point for those services.

I highly suggest taking some time to review both because they present options for anyone undertaking such a migration. Several
options are always welcome.

### How does the **Arkadiko** project come into play? ###

Arkadiko is a small bridge between Spring and OSGi.

Arkadiko is an attempt to provide an new migration option, and borrows ideas from both of the above and tries to marry into those the concepts of **Quick Wins** and **Time to Evolve**.

* **Quick Wins:** Often the light at the end of the tunnel seems awfully far away. When reviewing the scope of the migration, it may seem like an eternity before the benefits will pay off. So, **Arkadiko gives you OSGi right now!** How does it do that? It simply provides a means to wire an OSGi framework into your current spring container. But that in itself doesn't help and so it can also dynamically register all your beans as services and at the same time registers OSGi ServiceTrackers for beans matching specific exclusion/inclusion rules. It does this so that if matching services are published into the OSGi framework they can be automatically wired in place of those original beans. You get OSGi right away! It also means that the OSGi framework has access to all your beans and can use those as regular services.

* **Time to Evolve:** The other benefit is that you now have time to evolve your platform into OSGi as you see fit, and as time allows, moving components slowly from outside the OSGi framework, into the OSGi framework as re-design is completed by component. Also, those nasty libraries which have yet to be ported or are still known to not live happily inside of OSGi framework can remain outside the framework, consumed and wired from within the container, until such a time as they evolve their own OSGi solutions.

Licensed under the LGPL 2.1 or later (see LICENSE).

Arkadiko is very small and very simple! It only comprises a few classes.

Adding Arkadiko to your existing spring configurations is as simple as adding a few beans. See the fully documented example at examples/arkadiko-spring.xml .