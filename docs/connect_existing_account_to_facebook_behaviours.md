# Connect Existing Account To Facebook Behaviours

The server will return `userIdType`. There are 2 possibilities:

* `EMAIL` type -> user should log in using his email.
* `MOBILE` type -> user should log in using his phone number.

Do not rely on `userIdType`, this value might be removed or not given in the future.

If the `userIdType` is removed or the server doesn't return it, then the client can log in using either email or phone number.