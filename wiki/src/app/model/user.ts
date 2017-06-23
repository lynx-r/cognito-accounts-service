import {EnumOAuthType} from "./oauth-type.enum";
export interface User {
  name: string;
  email: string;
  provider: EnumOAuthType;
}
